import 'package:equatable/equatable.dart';
import 'package:campuscon/features/auth/models/university_model.dart';
import 'package:campuscon/features/auth/models/college_model.dart';

abstract class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object?> get props => [];
}

class AuthCheckRequested extends AuthEvent {}

class RoleSelected extends AuthEvent {
  final String role;

  const RoleSelected(this.role);

  @override
  List<Object?> get props => [role];
}

class StudentLoginRequested extends AuthEvent {
  final String usernameOrEmail;
  final String password;

  const StudentLoginRequested({
    required this.usernameOrEmail,
    required this.password,
  });

  @override
  List<Object?> get props => [usernameOrEmail, password];
}

class SocietyLoginRequested extends AuthEvent {
  final String email;
  final String password;

  const SocietyLoginRequested({
    required this.email,
    required this.password,
  });

  @override
  List<Object?> get props => [email, password];
}

class StudentRegistrationRequested extends AuthEvent {
  final String university;
  final String college;
  final String universityId;
  final String collegeId;
  final String email;
  final String name;
  final String course;
  final String courseId;
  final String batch;
  final String rollNumber;
  final String username;
  final String password;

  const StudentRegistrationRequested({
    required this.university,
    required this.college,
    required this.universityId,
    required this.collegeId,
    required this.email,
    required this.name,
    required this.course,
    required this.courseId,
    required this.batch,
    required this.rollNumber,
    required this.username,
    required this.password,
  });

  @override
  List<Object?> get props => [
        university,
        college,
        universityId,
        collegeId,
        email,
        name,
        course,
        courseId,
        batch,
        rollNumber,
        username,
        password,
      ];
}

class SocietyRegistrationRequested extends AuthEvent {
  final String university;
  final String college;
  final String universityId;
  final String collegeId;
  final String email;
  final String presidentName;
  final String societyName;
  final String username;
  final String password;

  const SocietyRegistrationRequested({
    required this.university,
    required this.college,
    required this.universityId,
    required this.collegeId,
    required this.email,
    required this.presidentName,
    required this.societyName,
    required this.username,
    required this.password,
  });

  @override
  List<Object?> get props => [
        university,
        college,
        universityId,
        collegeId,
        email,
        presidentName,
        societyName,
        username,
        password,
      ];
}

class SendOTPRequested extends AuthEvent {
  final String email;

  const SendOTPRequested({
    required this.email,
  });

  @override
  List<Object?> get props => [email];
}

class VerifyOTPRequested extends AuthEvent {
  final String email;
  final String otp;

  const VerifyOTPRequested({
    required this.email,
    required this.otp,
  });

  @override
  List<Object?> get props => [email, otp];
}

class LogoutRequested extends AuthEvent {}

class StoreTempData extends AuthEvent {
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

  const StoreTempData({
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

  @override
  List<Object?> get props => [
    tempUniversity, 
    tempCollege, 
    tempEmail, 
    tempSocietyName, 
    tempPresidentName,
    tempStudentName,
    tempBatch,
    tempCourse,
    tempRollNumber
  ];
}
