import 'package:flutter/foundation.dart';

import '../models/deed_model.dart';
import '../network/api_endpoints.dart';
import '../network/dio_client.dart';

class DeedRepository {
  final DioClient _dioClient = DioClient();

  // Get deeds from college
  Future<DeedResponse> getCollegeDeeds({
    required String collegeName,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dioClient.get(
        '${ApiEndpoints.collegeDeeds}/$collegeName',
        queryParameters: {'page': page, 'size': size},
      );

      return DeedResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching college deeds: $e');
      }
      rethrow;
    }
  }

  // Get popular deeds
  Future<DeedResponse> getPopularDeeds({int page = 0, int size = 20}) async {
    try {
      final response = await _dioClient.get(
        ApiEndpoints.popularDeeds,
        queryParameters: {'page': page, 'size': size},
      );

      return DeedResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching popular deeds: $e');
      }
      rethrow;
    }
  }

  // Get upcoming deeds
  Future<DeedResponse> getUpcomingDeeds({int page = 0, int size = 20}) async {
    try {
      final response = await _dioClient.get(
        ApiEndpoints.upcomingDeeds,
        queryParameters: {'page': page, 'size': size},
      );

      return DeedResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching upcoming deeds: $e');
      }
      rethrow;
    }
  }

  // Get deeds by category
  Future<DeedResponse> getDeedsByCategory({
    required String category,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dioClient.get(
        '${ApiEndpoints.deedsByCategory}/$category',
        queryParameters: {'page': page, 'size': size},
      );

      return DeedResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching deeds by category: $e');
      }
      rethrow;
    }
  }

  // Get deeds by society
  Future<DeedResponse> getSocietyDeeds({
    required String societyId,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dioClient.get(
        '${ApiEndpoints.societyDeeds}/$societyId',
        queryParameters: {'page': page, 'size': size},
      );

      return DeedResponse.fromJson(response.data['data']);
    } catch (e) {
      if (kDebugMode) {
        print('Error fetching society deeds: $e');
      }
      rethrow;
    }
  }

  // Like a deed
  Future<bool> likeDeed(String deedId) async {
    try {
      final response = await _dioClient.post(
        '${ApiEndpoints.deedLike}/$deedId/like',
      );

      return response.data['data'] ?? false;
    } catch (e) {
      if (kDebugMode) {
        print('Error liking deed: $e');
      }
      return false;
    }
  }

  // Save a deed
  Future<bool> saveDeed(String deedId) async {
    try {
      final response = await _dioClient.post(
        '${ApiEndpoints.deedSave}/$deedId/save',
      );

      return response.data['data'] ?? false;
    } catch (e) {
      if (kDebugMode) {
        print('Error saving deed: $e');
      }
      return false;
    }
  }

  // Register for a deed
  Future<bool> registerForDeed(String deedId) async {
    try {
      final response = await _dioClient.post(
        ApiEndpoints.deedRegistration,
        data: {'deedId': deedId},
      );

      return response.statusCode == 200;
    } catch (e) {
      if (kDebugMode) {
        print('Error registering for deed: $e');
      }
      return false;
    }
  }

  // Share a deed
  Future<bool> shareDeed(String deedId) async {
    try {
      final response = await _dioClient.post(
        '${ApiEndpoints.deedShare}/$deedId/share',
      );

      return response.statusCode == 200;
    } catch (e) {
      if (kDebugMode) {
        print('Error sharing deed: $e');
      }
      return false;
    }
  }
}
