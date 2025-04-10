import 'package:flutter/foundation.dart';

import '../models/brick_model.dart';
import '../network/api_endpoints.dart';
import '../network/dio_client.dart';

class BrickRepository {
  final DioClient _dioClient = DioClient();

  // Get bricks feed (from bonded users)
  Future<BrickResponse> getBricksFeed({int page = 0, int size = 20}) async {
    try {
      final response = await _dioClient.get(
        ApiEndpoints.bricksFeed,
        queryParameters: {'page': page, 'size': size},
      );

      return BrickResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching bricks feed: $e');
      }
      rethrow;
    }
  }

  // Get bricks from a specific user
  Future<BrickResponse> getUserBricks({
    required String userId,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dioClient.get(
        '${ApiEndpoints.userBricks}/$userId',
        queryParameters: {'page': page, 'size': size},
      );

      return BrickResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching user bricks: $e');
      }
      rethrow;
    }
  }

  // Get saved bricks
  Future<BrickResponse> getSavedBricks({int page = 0, int size = 20}) async {
    try {
      final response = await _dioClient.get(
        ApiEndpoints.savedBricks,
        queryParameters: {'page': page, 'size': size},
      );

      return BrickResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching saved bricks: $e');
      }
      rethrow;
    }
  }

  // Like a brick
  Future<bool> likeBrick(String brickId) async {
    try {
      final response = await _dioClient.post(
        '${ApiEndpoints.brickLike}/$brickId/like',
      );

      return response.data['data'] ?? false;
    } catch (e) {
      if (kDebugMode) {
        print('Error liking brick: $e');
      }
      return false;
    }
  }

  // Save a brick
  Future<bool> saveBrick(String brickId) async {
    try {
      final response = await _dioClient.post(
        '${ApiEndpoints.brickSave}/$brickId/save',
      );

      return response.data['data'] ?? false;
    } catch (e) {
      if (kDebugMode) {
        print('Error saving brick: $e');
      }
      return false;
    }
  }

  // Add a comment to a brick
  Future<bool> commentOnBrick(String brickId, String content) async {
    try {
      final response = await _dioClient.post(
        '${ApiEndpoints.brickComment}/$brickId/comment',
        data: {'content': content},
      );

      return response.statusCode == 200;
    } catch (e) {
      if (kDebugMode) {
        print('Error commenting on brick: $e');
      }
      return false;
    }
  }

  // Share a brick
  Future<bool> shareBrick(String brickId) async {
    try {
      final response = await _dioClient.post(
        '${ApiEndpoints.brickShare}/$brickId/share',
      );

      return response.statusCode == 200;
    } catch (e) {
      if (kDebugMode) {
        print('Error sharing brick: $e');
      }
      return false;
    }
  }
}
