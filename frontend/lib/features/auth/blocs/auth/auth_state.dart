import 'package:equatable/equatable.dart';
import 'package:campuscon/features/auth/models/university_model.dart';
import 'package:campuscon/features/auth/models/college_model.dart';
import 'package:campuscon/features/auth/models/course_model.dart';

enum AuthStatus {
  initial,
  loading,
  authenticated,
  unauthenticated,
  error,
  otpSent,
}

class AuthState extends Equatable {
  final AuthStatus status;
  final String? userRole;
  final String? token;
  final String? errorMessage;
  final bool isOtpVerified;
  
  // Temporary data for multi-step registration
  final University? tempUniversity;
  final College? tempCollege;
  final String? tempEmail;
  
  // Society registration specific fields
  final String? tempSocietyName;
  final String? tempPresidentName;
  
  // Student registration specific fields
  final String? tempStudentName;
  final String? tempBatch;
  final Course? tempCourse;
  final String? tempRollNumber;

  const AuthState({
    this.status = AuthStatus.initial,
    this.userRole,
    this.token,
    this.errorMessage,
    this.isOtpVerified = false,
    this.tempUniversity,
    this.tempCollege,
    this.tempEmail,
    this.tempSocietyName,
    this.tempPresidentName,
    this.tempStudentName,
    this.tempBatch,
    this.tempCourse,
    this.tempRollNumber,
  });

  bool get isAuthenticated =>
      status == AuthStatus.authenticated && token != null;
  bool get isLoading => status == AuthStatus.loading;
  bool get hasError => status == AuthStatus.error && errorMessage != null;
  bool get isOtpSent => status == AuthStatus.otpSent;

  AuthState copyWith({
    AuthStatus? status,
    String? userRole,
    String? token,
    String? errorMessage,
    bool? isOtpVerified,
    University? tempUniversity,
    College? tempCollege,
    String? tempEmail,
    String? tempSocietyName,
    String? tempPresidentName,
    String? tempStudentName,
    String? tempBatch,
    Course? tempCourse,
    String? tempRollNumber,
  }) {
    return AuthState(
      status: status ?? this.status,
      userRole: userRole ?? this.userRole,
      token: token ?? this.token,
      errorMessage: errorMessage ?? this.errorMessage,
      isOtpVerified: isOtpVerified ?? this.isOtpVerified,
      tempUniversity: tempUniversity ?? this.tempUniversity,
      tempCollege: tempCollege ?? this.tempCollege,
      tempEmail: tempEmail ?? this.tempEmail,
      tempSocietyName: tempSocietyName ?? this.tempSocietyName,
      tempPresidentName: tempPresidentName ?? this.tempPresidentName,
      tempStudentName: tempStudentName ?? this.tempStudentName,
      tempBatch: tempBatch ?? this.tempBatch,
      tempCourse: tempCourse ?? this.tempCourse,
      tempRollNumber: tempRollNumber ?? this.tempRollNumber,
    );
  }

  factory AuthState.initial() {
    return const AuthState(
      status: AuthStatus.initial,
      userRole: null,
      token: null,
      errorMessage: null,
      isOtpVerified: false,
      tempUniversity: null,
      tempCollege: null,
      tempEmail: null,
      tempSocietyName: null,
      tempPresidentName: null,
      tempStudentName: null,
      tempBatch: null,
      tempCourse: null,
      tempRollNumber: null,
    );
  }

  factory AuthState.loading() {
    return const AuthState(
      status: AuthStatus.loading,
      userRole: null,
      token: null,
      errorMessage: null,
      isOtpVerified: false,
      tempUniversity: null,
      tempCollege: null,
      tempEmail: null,
      tempSocietyName: null,
      tempPresidentName: null,
      tempStudentName: null,
      tempBatch: null,
      tempCourse: null,
      tempRollNumber: null,
    );
  }

  factory AuthState.authenticated(String userRole, String token) {
    return AuthState(
      status: AuthStatus.authenticated,
      userRole: userRole,
      token: token,
      errorMessage: null,
      isOtpVerified: true,
      tempUniversity: null,
      tempCollege: null,
      tempEmail: null,
      tempSocietyName: null,
      tempPresidentName: null,
      tempStudentName: null,
      tempBatch: null,
      tempCourse: null,
      tempRollNumber: null,
    );
  }

  factory AuthState.unauthenticated() {
    return const AuthState(
      status: AuthStatus.unauthenticated,
      userRole: null,
      token: null,
      errorMessage: null,
      isOtpVerified: false,
      tempUniversity: null,
      tempCollege: null,
      tempEmail: null,
      tempSocietyName: null,
      tempPresidentName: null,
      tempStudentName: null,
      tempBatch: null,
      tempCourse: null,
      tempRollNumber: null,
    );
  }

  factory AuthState.error(String message) {
    return AuthState(
      status: AuthStatus.error,
      userRole: null,
      token: null,
      errorMessage: message,
      isOtpVerified: false,
      tempUniversity: null,
      tempCollege: null,
      tempEmail: null,
      tempSocietyName: null,
      tempPresidentName: null,
      tempStudentName: null,
      tempBatch: null,
      tempCourse: null,
      tempRollNumber: null,
    );
  }

  factory AuthState.otpVerified() {
    return const AuthState(
      status: AuthStatus.unauthenticated,
      userRole: null,
      token: null,
      errorMessage: null,
      isOtpVerified: true,
      tempUniversity: null,
      tempCollege: null,
      tempEmail: null,
      tempSocietyName: null,
      tempPresidentName: null,
      tempStudentName: null,
      tempBatch: null,
      tempCourse: null,
      tempRollNumber: null,
    );
  }

  @override
  List<Object?> get props => [
    status,
    userRole,
    token,
    errorMessage,
    isOtpVerified,
    tempUniversity,
    tempCollege,
    tempSocietyName,
    tempEmail,
  ];
}
