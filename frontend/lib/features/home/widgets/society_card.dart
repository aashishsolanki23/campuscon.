import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../data/models/deed_model.dart';

class SocietyCard extends StatelessWidget {
  final Society society;
  final VoidCallback? onTap;
  final VoidCallback? onBondTap;

  const SocietyCard({
    super.key,
    required this.society,
    this.onTap,
    this.onBondTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Container(
        width: 120,
        margin: const EdgeInsets.only(right: 16),
        decoration: BoxDecoration(
          color: const Color(0xFF121212),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          children: [
            // Society Image
            ClipRRect(
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(16),
                topRight: Radius.circular(16),
              ),
              child: CachedNetworkImage(
                imageUrl: society.profilePictureUrl,
                height: 60,
                width: double.infinity,
                fit: BoxFit.cover,
                placeholder:
                    (context, url) => Container(
                      height: 60,
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
                      height: 60,
                      color: Colors.grey[900],
                      child: const Icon(
                        Icons.image_not_supported,
                        color: Colors.white,
                      ),
                    ),
              ),
            ),

            const SizedBox(height: 4),

            // Society Name
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4),
              child: Text(
                society.name,
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 12,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                textAlign: TextAlign.center,
              ),
            ),

            const SizedBox(height: 4),

            // Bond Button
            InkWell(
              onTap: onBondTap,
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 4,
                ),
                decoration: BoxDecoration(
                  color:
                      society.bonded
                          ? Colors.grey.withOpacity(0.3)
                          : const Color(0xFFA259FF),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  society.bonded ? 'Bonded' : 'Bond',
                  style: TextStyle(
                    color: society.bonded ? Colors.grey : Colors.white,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
