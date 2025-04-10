import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:campuscon/data/network/api_endpoints.dart';
import 'package:campuscon/data/network/dio_client.dart';
import 'package:campuscon/features/auth/models/auth_response.dart';
import 'package:campuscon/features/auth/models/university_model.dart';
import 'package:campuscon/features/auth/models/college_model.dart';
import 'package:campuscon/features/auth/models/course_model.dart';

class AuthRepository {
  late final Dio _dio;
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();
  
  AuthRepository() {
    _dio = DioClient.getInstance();
  }

  Future<AuthResponse> loginStudent({
    required String usernameOrEmail,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.studentLogin,
        data: jsonEncode({
          'usernameOrEmail': usernameOrEmail,
          'password': password,
        }),
      );

      return AuthResponse.fromJson(response.data);
    } on DioException catch (e) {
      return AuthResponse(
        success: false,
        message: e.response?.data['message'] ?? 'Login failed',
        token: null,
      );
    } catch (e) {
      return AuthResponse(
        success: false,
        message: 'An unexpected error occurred',
        token: null,
      );
    }
  }

  Future<AuthResponse> loginSociety({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.societyLogin,
        data: jsonEncode({
          'email': email,
          'password': password,
        }),
      );

      return AuthResponse.fromJson(response.data);
    } on DioException catch (e) {
      return AuthResponse(
        success: false,
        message: e.response?.data['message'] ?? 'Login failed',
        token: null,
      );
    } catch (e) {
      return AuthResponse(
        success: false,
        message: 'An unexpected error occurred',
        token: null,
      );
    }
  }

  Future<AuthResponse> registerStudent({
    required String university,
    required String college,
    required String universityId,
    required String collegeId,
    required String email,
    required String name,
    required String course,
    required String courseId,
    required String batch,
    required String rollNumber,
    required String username,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.studentRegister,
        data: jsonEncode({
          'university': university,
          'college': college,
          'universityId': universityId,
          'collegeId': collegeId,
          'email': email,
          'name': name,
          'course': course,
          'courseId': courseId,
          'batch': batch,
          'rollNumber': rollNumber,
          'username': username,
          'password': password,
        }),
      );

      return AuthResponse.fromJson(response.data);
    } on DioException catch (e) {
      return AuthResponse(
        success: false,
        message: e.response?.data['message'] ?? 'Registration failed',
        token: null,
      );
    } catch (e) {
      return AuthResponse(
        success: false,
        message: 'An unexpected error occurred',
        token: null,
      );
    }
  }

  Future<AuthResponse> registerSociety({
    required String university,
    required String college,
    required String universityId,
    required String collegeId,
    required String email,
    required String presidentName,
    required String societyName,
    required String username,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.societyRegister,
        data: jsonEncode({
          'university': university,
          'college': college,
          'universityId': universityId,
          'collegeId': collegeId,
          'email': email,
          'presidentName': presidentName,
          'societyName': societyName,
          'username': username,
          'password': password,
        }),
      );

      return AuthResponse.fromJson(response.data);
    } on DioException catch (e) {
      return AuthResponse(
        success: false,
        message: e.response?.data['message'] ?? 'Registration failed',
        token: null,
      );
    } catch (e) {
      return AuthResponse(
        success: false,
        message: 'An unexpected error occurred',
        token: null,
      );
    }
  }

  Future<AuthResponse> verifyOTP({
    required String email,
    required String otp,
  }) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.verifyOtp,
        data: jsonEncode({
          'email': email,
          'otp': otp,
        }),
      );

      return AuthResponse.fromJson(response.data);
    } on DioException catch (e) {
      return AuthResponse(
        success: false,
        message: e.response?.data['message'] ?? 'OTP verification failed',
        token: null,
      );
    } catch (e) {
      return AuthResponse(
        success: false,
        message: 'An unexpected error occurred',
        token: null,
      );
    }
  }

  Future<AuthResponse> sendOTP({required String email}) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.sendOtp,
        data: jsonEncode({
          'email': email,
        }),
      );

      return AuthResponse.fromJson(response.data);
    } on DioException catch (e) {
      return AuthResponse(
        success: false,
        message: e.response?.data['message'] ?? 'Failed to send OTP',
        token: null,
      );
    } catch (e) {
      return AuthResponse(
        success: false,
        message: 'An unexpected error occurred',
        token: null,
      );
    }
  }

  Future<AuthResponse> checkUsernameAvailability(String username) async {
    try {
      final response = await _dio.get(
        '${ApiEndpoints.baseUrl}/auth/check-username',
        queryParameters: {'username': username},
      );

      return AuthResponse.fromJson(response.data);
    } on DioException catch (e) {
      return AuthResponse(
        success: false,
        message: e.response?.data['message'] ?? 'Username check failed',
        token: null,
      );
    } catch (e) {
      return AuthResponse(
        success: false,
        message: 'An unexpected error occurred',
        token: null,
      );
    }
  }
  
  // Helper methods for token management
  Future<void> saveToken(String token) async {
    await _secureStorage.write(key: 'auth_token', value: token);
  }

  Future<String?> getToken() async {
    return await _secureStorage.read(key: 'auth_token');
  }

  Future<void> deleteToken() async {
    await _secureStorage.delete(key: 'auth_token');
    await _secureStorage.delete(key: 'userRole');
  }
  
  // Methods to fetch universities, colleges and courses
  Future<List<University>> getUniversities() async {
    try {
      final response = await _dio.get(ApiEndpoints.universities);
      final List<dynamic> data = response.data['data'] ?? [];
      return data.map((json) => University.fromJson(json)).toList();
    } catch (e) {
      print('Error fetching universities: $e');
      return [];
    }
  }

  Future<List<College>> getColleges({required String universityId}) async {
    try {
      final response = await _dio.get(
        '${ApiEndpoints.colleges}?universityId=$universityId',
      );
      final List<dynamic> data = response.data['data'] ?? [];
      return data.map((json) => College.fromJson(json)).toList();
    } catch (e) {
      print('Error fetching colleges: $e');
      return [];
    }
  }

  Future<List<Course>> getCourses({required String collegeId}) async {
    try {
      final response = await _dio.get(
        '${ApiEndpoints.courses}?collegeId=$collegeId',
      );
      final List<dynamic> data = response.data['data'] ?? [];
      return data.map((json) => Course.fromJson(json)).toList();
    } catch (e) {
      print('Error fetching courses: $e');
      return [];
    }
  }
  
  Future<bool> validateEmailDomain({required String email, required String collegeId}) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.validateEmailDomain,
        data: jsonEncode({
          'email': email,
          'collegeId': collegeId,
        }),
      );
      return response.data['valid'] ?? false;
    } catch (e) {
      print('Error validating email domain: $e');
      return false;
    }
  }
  
  // Check if user is logged in
  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }
  
  // Logout user and clear credentials
  Future<void> logout() async {
    await deleteToken();
    await _secureStorage.delete(key: 'userRole');
  }
}
