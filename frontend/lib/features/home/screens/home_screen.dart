import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';
import 'package:auto_route/auto_route.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_bloc.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_event.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_state.dart';
import 'package:campuscon/data/models/deed_model.dart';
import 'package:campuscon/data/models/brick_model.dart';
import 'package:campuscon/features/home/bloc/deeds/deeds_bloc.dart';
import 'package:campuscon/features/home/bloc/bricks/bricks_bloc.dart';
import 'package:campuscon/features/home/widgets/deed_card.dart';
import 'package:campuscon/features/home/widgets/society_card.dart';
import 'package:campuscon/features/home/widgets/brick_card.dart';
import 'package:cached_network_image/cached_network_image.dart';

@RoutePage()
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  final RefreshController _refreshController = RefreshController(initialRefresh: false);
  late TabController _tabController;
  
  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    
    // Load initial data
    context.read<DeedsBloc>().add(const LoadSocietyDeeds());
    context.read<DeedsBloc>().add(const LoadPopularDeeds());
    context.read<BricksBloc>().add(const LoadBricksFeed());
  }
  
  @override
  void dispose() {
    _refreshController.dispose();
    _tabController.dispose();
    super.dispose();
  }
  
  void _onRefresh() async {
    // Reload data
    context.read<DeedsBloc>().add(const LoadSocietyDeeds());
    context.read<DeedsBloc>().add(const LoadPopularDeeds());
    context.read<BricksBloc>().add(const LoadBricksFeed());
    _refreshController.refreshCompleted();
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF000000),
      body: SafeArea(
        child: SmartRefresher(
          controller: _refreshController,
          onRefresh: _onRefresh,
          header: const WaterDropHeader(
            waterDropColor: Color(0xFFA259FF),
            complete: Icon(Icons.check, color: Colors.white),
            completeDuration: Duration(milliseconds: 600),
          ),
          child: CustomScrollView(
            slivers: [
            // App Bar with Logo and Action Icons
            SliverAppBar(
              backgroundColor: Colors.transparent,
              floating: true,
              title: const Text(
                'CAMPUSCON.',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 24,
                ),
              ),
              actions: [
                // Bookmark Icon
                IconButton(
                  icon: const Icon(Icons.bookmark_border, color: Colors.white),
                  onPressed: () {},
                ),
                // Star Icon
                IconButton(
                  icon: const Icon(Icons.star_border, color: Colors.white),
                  onPressed: () {},
                ),
                // Profile Image
                Padding(
                  padding: const EdgeInsets.only(right: 16.0),
                  child: CircleAvatar(
                    radius: 16,
                    backgroundColor: const Color(0xFFA259FF),
                    child: ClipOval(
                      child: Image.network(
                        'https://via.placeholder.com/32',
                        width: 32,
                        height: 32,
                        fit: BoxFit.cover,
                        errorBuilder: (context, error, stackTrace) => 
                            const Icon(Icons.person, color: Colors.white),
                      ),
                    ),
                  ),
                ),
              ],
            ),
            
            // Content
            SliverList(
              delegate: SliverChildListDelegate([
                // Society Deeds Section
                _buildSectionHeader(
                  'Deeds of your society!', 
                  onActionPressed: () {
                    // Navigate to create deed screen
                  },
                ),
                const SizedBox(height: 12),
                SizedBox(
                  height: 220,
                  child: BlocBuilder<DeedsBloc, DeedsState>(
                    buildWhen: (previous, current) => 
                      current is DeedsLoaded && current.category == 'society',
                    builder: (context, state) {
                      if (state is DeedsLoading) {
                        return _buildLoadingDeedsHorizontal();
                      } else if (state is DeedsLoaded && state.category == 'society') {
                        return ListView.builder(
                          scrollDirection: Axis.horizontal,
                          itemCount: state.deeds.length,
                          padding: const EdgeInsets.symmetric(horizontal: 16),
                          itemBuilder: (context, index) => DeedCard(
                            deed: state.deeds[index],
                            onTap: () {
                              // Navigate to deed details
                            },
                          ),
                        );
                      } else {
                        return _buildEmptyDeedsHorizontal('No society deeds found');
                      }
                    },
                  ),
                ),
                
                const SizedBox(height: 24),
                
                // College Societies Section
                _buildSectionHeader('Find your college societies!', onActionPressed: () {}),
                const SizedBox(height: 12),
                SizedBox(
                  height: 100,
                  child: BlocBuilder<DeedsBloc, DeedsState>(
                    buildWhen: (previous, current) => 
                      current is DeedsLoaded && current.category == 'society',
                    builder: (context, state) {
                      if (state is DeedsLoading) {
                        return _buildLoadingSocietiesHorizontal();
                      } else if (state is DeedsLoaded && state.category == 'society') {
                        final societies = state.deeds
                          .map((deed) => deed.society)
                          .toSet()
                          .toList();
                        
                        return ListView.builder(
                          scrollDirection: Axis.horizontal,
                          itemCount: societies.length,
                          padding: const EdgeInsets.symmetric(horizontal: 16),
                          itemBuilder: (context, index) => SocietyCard(
                            society: societies[index],
                            onTap: () {
                              // Navigate to society details
                            },
                            onBondTap: () {
                              // Request to bond with society
                            },
                          ),
                        );
                      } else {
                        return _buildEmptySocietiesHorizontal('No societies found');
                      }
                    },
                  ),
                ),
                
                const SizedBox(height: 24),
                
                // Tab Bar for Selection
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                  child: TabBar(
                    controller: _tabController,
                    indicatorColor: const Color(0xFFA259FF),
                    labelColor: Colors.white,
                    unselectedLabelColor: Colors.grey,
                    tabs: const [
                      Tab(text: 'Popular Deeds'),
                      Tab(text: 'Bricks Feed'),
                    ],
                    onTap: (index) {
                      if (index == 0) {
                        context.read<DeedsBloc>().add(const LoadPopularDeeds());
                      } else {
                        context.read<BricksBloc>().add(const LoadBricksFeed());
                      }
                    },
                  ),
                ),
                
                // Tab Content
                SizedBox(
                  height: 600, // Fixed height for the tab content
                  child: TabBarView(
                    controller: _tabController,
                    children: [
                      // Popular Deeds Tab
                      _buildPopularDeedsGrid(),
                      
                      // Bricks Feed Tab
                      _buildBricksFeedGrid(),
                    ],
                  ),
                ),
                
                const SizedBox(height: 24),
              ]),
            ),
          ],
        ),
      )),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // Show dialog to choose between creating a brick or deed
          showModalBottomSheet(
            context: context,
            backgroundColor: const Color(0xFF121212),
            shape: const RoundedRectangleBorder(
              borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
            ),
            builder: (context) => Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text(
                    'Create',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  ListTile(
                    leading: const Icon(Icons.photo_library, color: Color(0xFFA259FF)),
                    title: const Text('New Brick', style: TextStyle(color: Colors.white)),
                    subtitle: const Text('Share a moment with your community', style: TextStyle(color: Colors.grey)),
                    onTap: () {
                      Navigator.pop(context);
                      // Navigate to create brick screen
                    },
                  ),
                  ListTile(
                    leading: const Icon(Icons.event, color: Color(0xFFA259FF)),
                    title: const Text('New Deed', style: TextStyle(color: Colors.white)),
                    subtitle: const Text('Create an event for your society', style: TextStyle(color: Colors.grey)),
                    onTap: () {
                      Navigator.pop(context);
                      // Navigate to create deed screen
                    },
                  ),
                ],
              ),
            ),
          );
        },
        backgroundColor: const Color(0xFFA259FF),
        child: const Icon(Icons.add),
      ),
    );
  }
  
  // Section Header Widget
  Widget _buildSectionHeader(String title, {required VoidCallback onActionPressed}) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            title,
            style: const TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: 18,
            ),
          ),
          title == 'Deeds of your society!' 
              ? ElevatedButton.icon(
                  onPressed: onActionPressed,
                  icon: const Icon(Icons.add, size: 16),
                  label: const Text('Create deed'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFA259FF),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(20),
                    ),
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  ),
                )
              : TextButton(
                  onPressed: onActionPressed,
                  child: const Text(
                    'See all',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 14,
                    ),
                  ),
                ),
        ],
      ),
    );
  }
  
  // Helper methods for Deed section
  Widget _buildLoadingDeedsHorizontal() {
    return ListView.builder(
      scrollDirection: Axis.horizontal,
      itemCount: 5,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      itemBuilder: (context, index) => Container(
        width: 240,
        margin: const EdgeInsets.only(right: 16),
        decoration: BoxDecoration(
          color: const Color(0xFF121212),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 120,
              decoration: BoxDecoration(
                color: Colors.grey[900],
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(16),
                  topRight: Radius.circular(16),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 24,
                        height: 24,
                        decoration: BoxDecoration(
                          color: Colors.grey[800],
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Container(
                        width: 100,
                        height: 12,
                        decoration: BoxDecoration(
                          color: Colors.grey[800],
                          borderRadius: BorderRadius.circular(6),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Container(
                    width: double.infinity,
                    height: 12,
                    decoration: BoxDecoration(
                      color: Colors.grey[800],
                      borderRadius: BorderRadius.circular(6),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Container(
                    width: 60,
                    height: 12,
                    decoration: BoxDecoration(
                      color: Colors.grey[800],
                      borderRadius: BorderRadius.circular(6),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
  
  Widget _buildEmptyDeedsHorizontal(String message) {
    return Center(
      child: Text(
        message,
        style: const TextStyle(
          color: Colors.white70,
          fontSize: 16,
        ),
      ),
    );
  }
              Positioned(
                top = 8,
                right = 8,
                child = Container(
                  padding: const EdgeInsets.all(4),
                  decoration: BoxDecoration(
                    color: Colors.black.withOpacity(0.6),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.bookmark,
                    color: Colors.white,
                    size: 20,
                  ),
                ),
              ),
            ],
          ),
          
          // Deed Content
          Padding(
            padding = const EdgeInsets.all(12),
            child = Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Society Info
                Row(
                  children: [
                    // Society Icon
                    ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Image.network(
                        'https://via.placeholder.com/24',
                        width: 24,
                        height: 24,
                        fit: BoxFit.cover,
                      ),
                    ),
                    const SizedBox(width: 8),
                    // Society Name
                    const Text(
                      ': Robotic',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                
                // Deed Description
                const Text(
                  'Learn about machine learning, Artificial Intelligence to better understanding to robotics.',
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 12,
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                
                const SizedBox(height: 8),
                
                // Date Info
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFF333333),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Row(
                        children: [
                          Icon(
                            Icons.calendar_today,
                            color: Colors.white70,
                            size: 10,
                          ),
                          SizedBox(width: 4),
                          Text(
                            '23 Mar',
                            style: TextStyle(
                              color: Colors.white70,
                              fontSize: 10,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
  
  // Helper methods for Societies section
  Widget _buildLoadingSocietiesHorizontal() {
    return ListView.builder(
      scrollDirection: Axis.horizontal,
      itemCount: 5,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      itemBuilder: (context, index) => Container(
        width: 120,
        margin: const EdgeInsets.only(right: 16),
        decoration: BoxDecoration(
          color: const Color(0xFF121212),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          children: [
            Container(
              height: 60,
              decoration: BoxDecoration(
                color: Colors.grey[900],
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(16),
                  topRight: Radius.circular(16),
                ),
              ),
            ),
            const SizedBox(height: 4),
            Container(
              width: 80,
              height: 12,
              decoration: BoxDecoration(
                color: Colors.grey[800],
                borderRadius: BorderRadius.circular(6),
              ),
            ),
            const SizedBox(height: 4),
            Container(
              width: 60,
              height: 20,
              decoration: BoxDecoration(
                color: Colors.grey[800],
                borderRadius: BorderRadius.circular(10),
              ),
            ),
          ],
        ),
      ),
    );
  }
  
  Widget _buildEmptySocietiesHorizontal(String message) {
    return Center(
      child: Text(
        message,
        style: const TextStyle(
          color: Colors.white70,
          fontSize: 16,
        ),
      ),
    );
  }
  }
  
  // Popular Deeds Grid
  Widget _buildPopularDeedsGrid() {
    return BlocBuilder<DeedsBloc, DeedsState>(
      buildWhen: (previous, current) => 
        current is DeedsLoaded && current.category == 'popular',
      builder: (context, state) {
        if (state is DeedsLoading) {
          return _buildLoadingGrid();
        } else if (state is DeedsLoaded && state.category == 'popular') {
          return SingleChildScrollView(
            physics: const NeverScrollableScrollPhysics(),
            child: GridView.builder(
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 0.8,
                crossAxisSpacing: 12,
                mainAxisSpacing: 12,
              ),
              padding: const EdgeInsets.symmetric(horizontal: 16),
              physics: const NeverScrollableScrollPhysics(),
              shrinkWrap: true,
              itemCount: state.deeds.length,
              itemBuilder: (context, index) => DeedCard(
                deed: state.deeds[index],
                isDetailed: true,
                onTap: () {
                  // Navigate to deed details
                },
              ),
            ),
          );
        } else {
          return const Center(
            child: Text(
              'No popular deeds found',
              style: TextStyle(
                color: Colors.white70,
                fontSize: 16,
              ),
            ),
          );
        }
      },
    );
  }
  
  // Bricks Feed Grid
  Widget _buildBricksFeedGrid() {
    return BlocBuilder<BricksBloc, BricksState>(
      buildWhen: (previous, current) => 
        current is BricksLoaded && current.category == 'feed',
      builder: (context, state) {
        if (state is BricksLoading) {
          return _buildLoadingGrid();
        } else if (state is BricksLoaded && state.category == 'feed') {
          return SingleChildScrollView(
            physics: const NeverScrollableScrollPhysics(),
            child: GridView.builder(
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2,
                childAspectRatio: 0.8,
                crossAxisSpacing: 12,
                mainAxisSpacing: 12,
              ),
              padding: const EdgeInsets.symmetric(horizontal: 16),
              physics: const NeverScrollableScrollPhysics(),
              shrinkWrap: true,
              itemCount: state.bricks.length,
              itemBuilder: (context, index) => BrickCard(
                brick: state.bricks[index],
                onTap: () {
                  // Navigate to brick details
                },
              ),
            ),
          );
        } else {
          return const Center(
            child: Text(
              'No bricks found',
              style: TextStyle(
                color: Colors.white70,
                fontSize: 16,
              ),
            ),
          );
        }
      },
    );
  }
  
  Widget _buildLoadingGrid() {
    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        childAspectRatio: 0.8,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16),
      physics: const NeverScrollableScrollPhysics(),
      shrinkWrap: true,
      itemCount: 6,
      itemBuilder: (context, index) => Container(
        decoration: BoxDecoration(
          color: const Color(0xFF121212),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 100,
              decoration: BoxDecoration(
                color: Colors.grey[900],
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(16),
                  topRight: Radius.circular(16),
                ),
              ),
            ),
              Positioned(
                top: 8,
                right: 8,
                child: Container(
                  padding: const EdgeInsets.all(4),
                  decoration: BoxDecoration(
                    color: Colors.black.withOpacity(0.6),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.bookmark,
                    color: Colors.red,
                    size: 20,
                  ),
                ),
              ),
            ],
          ),
          
          // Content
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Society Info
                Row(
                  children: [
                    // Society Icon
                    ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Image.network(
                        'https://via.placeholder.com/20',
                        width: 20,
                        height: 20,
                        fit: BoxFit.cover,
                      ),
                    ),
                    const SizedBox(width: 4),
                    // Society Name
                    const Text(
                      ': Robotic',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                
                // Description
                const Text(
                  'Learn about machine learning, AI to better robotics.',
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 10,
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                
                const SizedBox(height: 4),
                
                // Date
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: const Color(0xFF333333),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Row(
                        children: [
                          Icon(
                            Icons.calendar_today,
                            color: Colors.white70,
                            size: 8,
                          ),
                          SizedBox(width: 2),
                          Text(
                            '23',
                            style: TextStyle(
                              color: Colors.white70,
                              fontSize: 8,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
