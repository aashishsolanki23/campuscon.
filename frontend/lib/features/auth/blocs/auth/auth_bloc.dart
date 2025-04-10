import 'package:hydrated_bloc/hydrated_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_event.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_state.dart';
import 'package:campuscon/features/auth/repository/auth_repository.dart';

class AuthBloc extends HydratedBloc<AuthEvent, AuthState> {
  final AuthRepository _authRepository;
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  AuthBloc({required AuthRepository authRepository})
    : _authRepository = authRepository,
      super(AuthState.initial()) {
    // Initial auth check
    add(AuthCheckRequested());

    on<AuthCheckRequested>(_onAuthCheckRequested);
    on<RoleSelected>(_onRoleSelected);
    on<StudentLoginRequested>(_onStudentLoginRequested);
    on<SocietyLoginRequested>(_onSocietyLoginRequested);
    on<StudentRegistrationRequested>(_onStudentRegistrationRequested);
    on<SocietyRegistrationRequested>(_onSocietyRegistrationRequested);
    on<SendOTPRequested>(_onSendOTPRequested);
    on<VerifyOTPRequested>(_onVerifyOTPRequested);
    on<LogoutRequested>(_onLogoutRequested);
    on<StoreTempData>(_onStoreTempData);
  }

  void _onAuthCheckRequested(
    AuthCheckRequested event,
    Emitter<AuthState> emit,
  ) async {
    // Check token from auth repository instead of direct secure storage access
    final token = await _authRepository.getToken();
    final userRole = await _secureStorage.read(key: 'userRole');

    if (token != null && userRole != null) {
      emit(AuthState.authenticated(userRole, token));
    } else {
      emit(AuthState.unauthenticated());
    }
  }

  void _onRoleSelected(RoleSelected event, Emitter<AuthState> emit) {
    emit(state.copyWith(userRole: event.role));
  }

  void _onStudentLoginRequested(
    StudentLoginRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthState.loading());
    try {
      final response = await _authRepository.loginStudent(
        usernameOrEmail: event.usernameOrEmail,
        password: event.password,
      );

      if (response.success) {
        await _authRepository.saveToken(response.token!);
        await _secureStorage.write(key: 'userRole', value: 'student');
        emit(AuthState.authenticated('student', response.token!));
      } else {
        emit(AuthState.error(response.message ?? 'Login failed'));
      }
    } catch (e) {
      emit(AuthState.error('An error occurred: ${e.toString()}'));
    }
  }

  void _onSocietyLoginRequested(
    SocietyLoginRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthState.loading());
    try {
      final response = await _authRepository.loginSociety(
        email: event.email,
        password: event.password,
      );

      if (response.success) {
        await _authRepository.saveToken(response.token!);
        await _secureStorage.write(key: 'userRole', value: 'society');
        emit(AuthState.authenticated('society', response.token!));
      } else {
        emit(AuthState.error(response.message ?? 'Login failed'));
      }
    } catch (e) {
      emit(AuthState.error('An error occurred: ${e.toString()}'));
    }
  }

  void _onStudentRegistrationRequested(
    StudentRegistrationRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthState.loading());
    try {
      final response = await _authRepository.registerStudent(
        university: event.university,
        college: event.college,
        universityId: event.universityId,
        collegeId: event.collegeId,
        email: event.email,
        name: event.name,
        course: event.course,
        courseId: event.courseId,
        batch: event.batch,
        rollNumber: event.rollNumber,
        username: event.username,
        password: event.password,
      );

      if (response.success) {
        // Email verification might be needed here
        emit(
          state.copyWith(
            status: AuthStatus.unauthenticated,
            errorMessage: null,
          ),
        );
      } else {
        emit(AuthState.error(response.message ?? 'Registration failed'));
      }
    } catch (e) {
      emit(AuthState.error('An error occurred: ${e.toString()}'));
    }
  }

  void _onSocietyRegistrationRequested(
    SocietyRegistrationRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthState.loading());
    try {
      final response = await _authRepository.registerSociety(
        university: event.university,
        college: event.college,
        universityId: event.universityId,
        collegeId: event.collegeId,
        email: event.email,
        presidentName: event.presidentName,
        societyName: event.societyName,
        username: event.username,
        password: event.password,
      );

      if (response.success) {
        // Email verification might be needed here
        emit(
          state.copyWith(
            status: AuthStatus.unauthenticated,
            errorMessage: null,
          ),
        );
      } else {
        emit(AuthState.error(response.message ?? 'Registration failed'));
      }
    } catch (e) {
      emit(AuthState.error('An error occurred: ${e.toString()}'));
    }
  }

  void _onSendOTPRequested(
    SendOTPRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthState.loading());
    try {
      final response = await _authRepository.sendOTP(email: event.email);

      if (response.success) {
        emit(state.copyWith(status: AuthStatus.otpSent, errorMessage: null));
      } else {
        emit(AuthState.error(response.message ?? 'Failed to send OTP'));
      }
    } catch (e) {
      emit(AuthState.error('An error occurred: ${e.toString()}'));
    }
  }

  void _onVerifyOTPRequested(
    VerifyOTPRequested event,
    Emitter<AuthState> emit,
  ) async {
    emit(AuthState.loading());
    try {
      final response = await _authRepository.verifyOTP(
        email: event.email,
        otp: event.otp,
      );

      if (response.success) {
        emit(AuthState.otpVerified());
      } else {
        emit(AuthState.error(response.message ?? 'OTP verification failed'));
      }
    } catch (e) {
      emit(AuthState.error('An error occurred: ${e.toString()}'));
    }
  }

  void _onLogoutRequested(
    LogoutRequested event,
    Emitter<AuthState> emit,
  ) async {
    await _authRepository.logout();
    emit(AuthState.unauthenticated());
  }

  void _onStoreTempData(StoreTempData event, Emitter<AuthState> emit) {
    emit(state.copyWith(
      tempUniversity: event.tempUniversity,
      tempCollege: event.tempCollege,
      tempEmail: event.tempEmail,
      tempSocietyName: event.tempSocietyName,
      tempPresidentName: event.tempPresidentName,
      tempStudentName: event.tempStudentName,
      tempBatch: event.tempBatch,
      tempCourse: event.tempCourse,
      tempRollNumber: event.tempRollNumber,
    ));
  }

  @override
  AuthState? fromJson(Map<String, dynamic> json) {
    try {
      return AuthState(
        status: AuthStatus.values.byName(json['status']),
        userRole: json['userRole'],
        token: json['token'],
        errorMessage: json['errorMessage'],
        isOtpVerified: json['isOtpVerified'] ?? false,
      );
    } catch (e) {
      return AuthState.initial();
    }
  }

  @override
  Map<String, dynamic>? toJson(AuthState state) {
    return {
      'status': state.status.name,
      'userRole': state.userRole,
      'token': state.token,
      'errorMessage': state.errorMessage,
      'isOtpVerified': state.isOtpVerified,
    };
  }
}
