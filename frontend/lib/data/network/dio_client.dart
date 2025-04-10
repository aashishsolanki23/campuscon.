import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class DioClient {
  final Dio _dio = Dio();
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  static final DioClient _instance = DioClient._internal();

  factory DioClient() {
    return _instance;
  }

  static Dio getInstance() {
    return _instance._dio;
  }

  DioClient._internal() {
    _dio.options.baseUrl = 'https://api.campuscon.com/api';
    _dio.options.connectTimeout = const Duration(seconds: 20);
    _dio.options.receiveTimeout = const Duration(seconds: 20);
    _dio.options.contentType = Headers.jsonContentType;
    _dio.options.responseType = ResponseType.json;

    // Add interceptors
    _dio.interceptors.add(
      LogInterceptor(
        request: true,
        requestHeader: true,
        requestBody: true,
        responseHeader: true,
        responseBody: true,
        error: true,
        logPrint: (object) {
          if (kDebugMode) {
            print('DIO LOG: $object');
          }
        },
      ),
    );

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          // Add auth token to request if available
          final token = await _secureStorage.read(key: 'auth_token');
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          return handler.next(options);
        },
        onError: (DioException e, handler) async {
          if (e.response?.statusCode == 401) {
            // Token expired, try to refresh
            if (await _refreshToken()) {
              // Retry the original request
              return handler.resolve(await _retryRequest(e.requestOptions));
            }
          }
          return handler.next(e);
        },
      ),
    );
  }

  // Helper to retry a failed request with refreshed token
  Future<Response<dynamic>> _retryRequest(RequestOptions requestOptions) async {
    final token = await _secureStorage.read(key: 'auth_token');
    requestOptions.headers['Authorization'] = 'Bearer $token';

    final options = Options(
      method: requestOptions.method,
      headers: requestOptions.headers,
    );

    return _dio.request<dynamic>(
      requestOptions.path,
      data: requestOptions.data,
      queryParameters: requestOptions.queryParameters,
      options: options,
    );
  }

  // Token refresh functionality
  Future<bool> _refreshToken() async {
    try {
      final refreshToken = await _secureStorage.read(key: 'refresh_token');
      if (refreshToken == null) return false;

      final response = await _dio.post(
        '/auth/token/refresh',
        data: {'refreshToken': refreshToken},
        options: Options(headers: {'Authorization': null}),
      );

      if (response.statusCode == 200) {
        await _secureStorage.write(
          key: 'auth_token',
          value: response.data['data']['token'],
        );
        await _secureStorage.write(
          key: 'refresh_token',
          value: response.data['data']['refreshToken'],
        );
        return true;
      }
      return false;
    } on DioException {
      // If refresh fails, clear tokens and require user to login again
      await _secureStorage.delete(key: 'auth_token');
      await _secureStorage.delete(key: 'refresh_token');
      return false;
    }
  }

  // GET request
  Future<Response> get(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    void Function(int, int)? onReceiveProgress,
  }) async {
    try {
      return await _dio.get(
        path,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onReceiveProgress: onReceiveProgress,
      );
    } on DioException catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  // POST request
  Future<Response> post(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    void Function(int, int)? onSendProgress,
    void Function(int, int)? onReceiveProgress,
  }) async {
    try {
      return await _dio.post(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onSendProgress: onSendProgress,
        onReceiveProgress: onReceiveProgress,
      );
    } on DioException catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  // PUT request
  Future<Response> put(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    void Function(int, int)? onSendProgress,
    void Function(int, int)? onReceiveProgress,
  }) async {
    try {
      return await _dio.put(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onSendProgress: onSendProgress,
        onReceiveProgress: onReceiveProgress,
      );
    } on DioException catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  // DELETE request
  Future<Response> delete(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
  }) async {
    try {
      return await _dio.delete(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
      );
    } on DioException catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  // PATCH request
  Future<Response> patch(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    void Function(int, int)? onSendProgress,
    void Function(int, int)? onReceiveProgress,
  }) async {
    try {
      return await _dio.patch(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
        cancelToken: cancelToken,
        onSendProgress: onSendProgress,
        onReceiveProgress: onReceiveProgress,
      );
    } on DioException catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  // Handle upload with FormData
  Future<Response> upload(
    String path, {
    required FormData formData,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    void Function(int, int)? onSendProgress,
    void Function(int, int)? onReceiveProgress,
  }) async {
    try {
      return await _dio.post(
        path,
        data: formData,
        queryParameters: queryParameters,
        options: options ?? Options(contentType: 'multipart/form-data'),
        cancelToken: cancelToken,
        onSendProgress: onSendProgress,
        onReceiveProgress: onReceiveProgress,
      );
    } on DioException catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  // Download file
  Future<Response> download(
    String url,
    String savePath, {
    void Function(int, int)? onReceiveProgress,
    Map<String, dynamic>? queryParameters,
    CancelToken? cancelToken,
    bool deleteOnError = true,
    Options? options,
  }) async {
    try {
      return await _dio.download(
        url,
        savePath,
        onReceiveProgress: onReceiveProgress,
        queryParameters: queryParameters,
        cancelToken: cancelToken,
        deleteOnError: deleteOnError,
        options: options,
      );
    } on DioException catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  // Handle errors
  void _handleError(DioException error) {
    if (kDebugMode) {
      print('DIO ERROR: ${error.message}');
    }

    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        throw DeadlineExceededException(error.requestOptions);
      case DioExceptionType.badResponse:
        switch (error.response?.statusCode) {
          case 400:
            throw BadRequestException(error.requestOptions, error.response);
          case 401:
            throw UnauthorizedException(error.requestOptions, error.response);
          case 403:
            throw ForbiddenException(error.requestOptions, error.response);
          case 404:
            throw NotFoundException(error.requestOptions, error.response);
          case 409:
            throw ConflictException(error.requestOptions, error.response);
          case 500:
            throw InternalServerErrorException(
              error.requestOptions,
              error.response,
            );
        }
        throw BadResponseException(error.requestOptions, error.response);
      case DioExceptionType.cancel:
        throw RequestCancelledException(error.requestOptions);
      case DioExceptionType.unknown:
        if (error.error is SocketException) {
          throw NoInternetConnectionException(error.requestOptions);
        }
        throw UnknownException(error.requestOptions, error);
      default:
        throw error;
    }
  }
}

// Custom exceptions
class AppException extends DioException {
  AppException(RequestOptions requestOptions)
    : super(requestOptions: requestOptions);
}

class BadRequestException extends AppException {
  BadRequestException(super.requestOptions, Response? response);
}

class UnauthorizedException extends AppException {
  UnauthorizedException(super.requestOptions, Response? response);
}

class ForbiddenException extends AppException {
  ForbiddenException(super.requestOptions, Response? response);
}

class NotFoundException extends AppException {
  NotFoundException(super.requestOptions, Response? response);
}

class ConflictException extends AppException {
  ConflictException(super.requestOptions, Response? response);
}

class InternalServerErrorException extends AppException {
  InternalServerErrorException(super.requestOptions, Response? response);
}

class NoInternetConnectionException extends AppException {
  NoInternetConnectionException(super.requestOptions);
}

class DeadlineExceededException extends AppException {
  DeadlineExceededException(super.requestOptions);
}

class RequestCancelledException extends AppException {
  RequestCancelledException(super.requestOptions);
}

class BadResponseException extends AppException {
  BadResponseException(super.requestOptions, Response? response);
}

class UnknownException extends AppException {
  UnknownException(super.requestOptions, dynamic error);
}
