import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';

import '../../../data/models/brick_model.dart';
import '../bloc/bricks/bricks_bloc.dart';

class BrickCard extends StatelessWidget {
  final Brick brick;
  final VoidCallback? onTap;
  final bool isInGrid;

  const BrickCard({
    super.key,
    required this.brick,
    this.onTap,
    this.isInGrid = true,
  });

  @override
  Widget build(BuildContext context) {
    if (isInGrid) {
      return _buildGridCard(context);
    } else {
      return _buildListCard(context);
    }
  }

  Widget _buildGridCard(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: const Color(0xFF121212),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image with Bookmark
            Stack(
              children: [
                ClipRRect(
                  borderRadius: const BorderRadius.only(
                    topLeft: Radius.circular(16),
                    topRight: Radius.circular(16),
                  ),
                  child: CachedNetworkImage(
                    imageUrl: brick.imageUrl,
                    height: 100,
                    width: double.infinity,
                    fit: BoxFit.cover,
                    placeholder:
                        (context, url) => Container(
                          height: 100,
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
                          height: 100,
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
                      context.read<BricksBloc>().add(SaveBrick(brick.id));
                    },
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: BoxDecoration(
                        color: Colors.black.withOpacity(0.6),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        brick.saved ? Icons.bookmark : Icons.bookmark_border,
                        color:
                            brick.saved
                                ? const Color(0xFFA259FF)
                                : Colors.white,
                        size: 20,
                      ),
                    ),
                  ),
                ),
              ],
            ),

            // Content
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // User Info
                  Row(
                    children: [
                      // User Avatar
                      CircleAvatar(
                        radius: 12,
                        backgroundColor: const Color(0xFFA259FF),
                        backgroundImage: CachedNetworkImageProvider(
                          brick.user.profilePictureUrl,
                        ),
                      ),
                      const SizedBox(width: 8),
                      // Username
                      Expanded(
                        child: Text(
                          brick.user.username,
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),

                  // Title/Description
                  Text(
                    brick.title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    brick.description,
                    style: const TextStyle(color: Colors.white70, fontSize: 12),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),

                  const SizedBox(height: 8),

                  // Action Row
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      // Like Button
                      InkWell(
                        onTap: () {
                          context.read<BricksBloc>().add(LikeBrick(brick.id));
                        },
                        child: Row(
                          children: [
                            Icon(
                              brick.liked
                                  ? Icons.favorite
                                  : Icons.favorite_border,
                              color: brick.liked ? Colors.red : Colors.white,
                              size: 16,
                            ),
                            const SizedBox(width: 4),
                            Text(
                              '${brick.likesCount}',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),

                      // Comment Button
                      InkWell(
                        onTap: () {
                          // Navigate to comments screen
                        },
                        child: Row(
                          children: [
                            const Icon(
                              Icons.comment_outlined,
                              color: Colors.white,
                              size: 16,
                            ),
                            const SizedBox(width: 4),
                            Text(
                              '${brick.commentsCount}',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),

                      // Share Button
                      InkWell(
                        onTap: () {
                          context.read<BricksBloc>().add(ShareBrick(brick.id));
                        },
                        child: const Icon(
                          Icons.share_outlined,
                          color: Colors.white,
                          size: 16,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildListCard(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(bottom: 16),
        decoration: BoxDecoration(
          color: const Color(0xFF121212),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Brick Image
            ClipRRect(
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(16),
                bottomLeft: Radius.circular(16),
              ),
              child: CachedNetworkImage(
                imageUrl: brick.imageUrl,
                height: 120,
                width: 120,
                fit: BoxFit.cover,
                placeholder:
                    (context, url) => Container(
                      height: 120,
                      width: 120,
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
                      width: 120,
                      color: Colors.grey[900],
                      child: const Icon(
                        Icons.image_not_supported,
                        color: Colors.white,
                      ),
                    ),
              ),
            ),

            // Content
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // User Info
                    Row(
                      children: [
                        // User Avatar
                        CircleAvatar(
                          radius: 12,
                          backgroundColor: const Color(0xFFA259FF),
                          backgroundImage: CachedNetworkImageProvider(
                            brick.user.profilePictureUrl,
                          ),
                        ),
                        const SizedBox(width: 8),
                        // Username
                        Expanded(
                          child: Text(
                            brick.user.username,
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),

                        // Save Button
                        InkWell(
                          onTap: () {
                            context.read<BricksBloc>().add(SaveBrick(brick.id));
                          },
                          child: Icon(
                            brick.saved
                                ? Icons.bookmark
                                : Icons.bookmark_border,
                            color:
                                brick.saved
                                    ? const Color(0xFFA259FF)
                                    : Colors.white,
                            size: 20,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),

                    // Title/Description
                    Text(
                      brick.title,
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      brick.description,
                      style: const TextStyle(
                        color: Colors.white70,
                        fontSize: 12,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),

                    const SizedBox(height: 12),

                    // Action Row
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        // Like Button
                        InkWell(
                          onTap: () {
                            context.read<BricksBloc>().add(LikeBrick(brick.id));
                          },
                          child: Row(
                            children: [
                              Icon(
                                brick.liked
                                    ? Icons.favorite
                                    : Icons.favorite_border,
                                color: brick.liked ? Colors.red : Colors.white,
                                size: 16,
                              ),
                              const SizedBox(width: 4),
                              Text(
                                '${brick.likesCount}',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),

                        // Comment Button
                        InkWell(
                          onTap: () {
                            // Navigate to comments screen
                          },
                          child: Row(
                            children: [
                              const Icon(
                                Icons.comment_outlined,
                                color: Colors.white,
                                size: 16,
                              ),
                              const SizedBox(width: 4),
                              Text(
                                '${brick.commentsCount}',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),

                        // Share Button
                        InkWell(
                          onTap: () {
                            context.read<BricksBloc>().add(
                              ShareBrick(brick.id),
                            );
                          },
                          child: const Icon(
                            Icons.share_outlined,
                            color: Colors.white,
                            size: 16,
                          ),
                        ),
                      ],
                    ),

                    // Date at bottom
                    Align(
                      alignment: Alignment.bottomRight,
                      child: Padding(
                        padding: const EdgeInsets.only(top: 8),
                        child: Text(
                          DateFormat('MMM dd, yyyy').format(brick.createdAt),
                          style: const TextStyle(
                            color: Colors.grey,
                            fontSize: 10,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
