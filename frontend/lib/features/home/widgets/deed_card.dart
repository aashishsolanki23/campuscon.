import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';

import '../../../data/models/deed_model.dart';
import '../bloc/deeds/deeds_bloc.dart';

class DeedCard extends StatelessWidget {
  final Deed deed;
  final bool isDetailed;
  final VoidCallback? onTap;

  const DeedCard({
    super.key,
    required this.deed,
    this.isDetailed = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        width: 240,
        margin: const EdgeInsets.only(right: 16),
        decoration: BoxDecoration(
          color: const Color(0xFF121212),
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.2),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Deed Image with Bookmark
            Stack(
              children: [
                ClipRRect(
                  borderRadius: const BorderRadius.only(
                    topLeft: Radius.circular(16),
                    topRight: Radius.circular(16),
                  ),
                  child: CachedNetworkImage(
                    imageUrl: deed.bannerUrl,
                    height: 120,
                    width: double.infinity,
                    fit: BoxFit.cover,
                    placeholder:
                        (context, url) => Container(
                          height: 120,
                          color: Colors.grey[900],
                          child: const Center(
                            child: CircularProgressIndicator(
                              valueColor: AlwaysStoppedAnimation<Color>(
                                Color(0xFFA259FF),
                              ),
                            ),
                          ),
                        ),
                    errorWidget:
                        (context, url, error) => Container(
                          height: 120,
                          color: Colors.grey[900],
                          child: const Icon(
                            Icons.image_not_supported,
                            color: Colors.white,
                          ),
                        ),
                  ),
                ),
                Positioned(
                  top: 8,
                  right: 8,
                  child: InkWell(
                    onTap: () {
                      context.read<DeedsBloc>().add(SaveDeed(deed.id));
                    },
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: BoxDecoration(
                        color: Colors.black.withOpacity(0.6),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        deed.saved ? Icons.bookmark : Icons.bookmark_border,
                        color:
                            deed.saved ? const Color(0xFFA259FF) : Colors.white,
                        size: 20,
                      ),
                    ),
                  ),
                ),
              ],
            ),

            // Deed Content
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Society Info
                  Row(
                    children: [
                      // Society Icon
                      ClipRRect(
                        borderRadius: BorderRadius.circular(12),
                        child: CachedNetworkImage(
                          imageUrl: deed.society.profilePictureUrl,
                          width: 24,
                          height: 24,
                          fit: BoxFit.cover,
                          placeholder:
                              (context, url) => Container(
                                width: 24,
                                height: 24,
                                color: Colors.grey[900],
                              ),
                          errorWidget:
                              (context, url, error) => Container(
                                width: 24,
                                height: 24,
                                color: Colors.grey[900],
                                child: const Icon(
                                  Icons.person,
                                  color: Colors.white,
                                  size: 12,
                                ),
                              ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      // Society Name
                      Expanded(
                        child: Text(
                          ': ${deed.society.name}',
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                            fontSize: 14,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),

                  // Deed Title
                  Text(
                    deed.title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),

                  const SizedBox(height: 4),

                  // Deed Description
                  Text(
                    deed.description,
                    style: const TextStyle(color: Colors.white70, fontSize: 12),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),

                  const SizedBox(height: 8),

                  // Deed Meta Info
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      // Category pill
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 4,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFF333333),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          deed.category,
                          style: const TextStyle(
                            color: Colors.white70,
                            fontSize: 10,
                          ),
                        ),
                      ),

                      // Date Info
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 4,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFF333333),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Row(
                          children: [
                            const Icon(
                              Icons.calendar_today,
                              color: Colors.white70,
                              size: 10,
                            ),
                            const SizedBox(width: 4),
                            Text(
                              DateFormat('dd MMM').format(deed.eventDate),
                              style: const TextStyle(
                                color: Colors.white70,
                                fontSize: 10,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),

                  if (isDetailed) ...[
                    const SizedBox(height: 12),

                    // Action buttons
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        // Like button
                        _buildActionButton(
                          icon:
                              deed.liked
                                  ? Icons.favorite
                                  : Icons.favorite_border,
                          color: deed.liked ? Colors.red : Colors.white,
                          label: '${deed.likesCount}',
                          onTap: () {
                            context.read<DeedsBloc>().add(LikeDeed(deed.id));
                          },
                        ),

                        // Comment button
                        _buildActionButton(
                          icon: Icons.comment_outlined,
                          label: '${deed.commentsCount}',
                          onTap: () {
                            // Navigate to comments
                          },
                        ),

                        // Share button
                        _buildActionButton(
                          icon: Icons.share_outlined,
                          label: 'Share',
                          onTap: () {
                            context.read<DeedsBloc>().add(ShareDeed(deed.id));
                          },
                        ),

                        // Register button
                        _buildActionButton(
                          icon:
                              deed.registered
                                  ? Icons.check_circle
                                  : Icons.app_registration,
                          color: deed.registered ? Colors.green : Colors.white,
                          label: deed.registered ? 'Registered' : 'Register',
                          onTap: () {
                            context.read<DeedsBloc>().add(
                              RegisterForDeed(deed.id),
                            );
                          },
                        ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButton({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
    Color color = Colors.white,
  }) {
    return InkWell(
      onTap: onTap,
      child: Column(
        children: [
          Icon(icon, color: color, size: 18),
          const SizedBox(height: 4),
          Text(label, style: TextStyle(color: color, fontSize: 10)),
        ],
      ),
    );
  }
}
