import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_bloc.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_event.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_state.dart';
import 'package:campuscon/features/auth/screens/common/custom_text_field.dart';
import 'package:campuscon/routes/app_router.dart';

@RoutePage()
class CredentialsScreen extends StatefulWidget {
  final bool isStudent; // To differentiate between student and society

  const CredentialsScreen({
    super.key, 
    required this.isStudent,
  });

  @override
  State<CredentialsScreen> createState() => _CredentialsScreenState();
}

class _CredentialsScreenState extends State<CredentialsScreen> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AuthBloc, AuthState>(
      listenWhen: (previous, current) => 
          previous.status != current.status || 
          previous.errorMessage != current.errorMessage,
      listener: (context, state) {
        if (state.isAuthenticated) {
          context.router.replace(const HomeRoute());
        } else if (state.hasError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(state.errorMessage!)),
          );
        }
      },
      child: Scaffold(
        backgroundColor: Colors.black,
        appBar: AppBar(
          backgroundColor: Colors.transparent,
          elevation: 0,
          title: Text(
            'Create Credentials',
            style: TextStyle(
              color: Colors.white,
              fontSize: 20.sp,
            ),
          ),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back, color: Colors.white),
            onPressed: () => context.router.pop(),
          ),
        ),
        body: SafeArea(
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 24.w),
            child: Form(
              key: _formKey,
              child: SingleChildScrollView(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    SizedBox(height: 20.h),
                    Text(
                      'Account Credentials',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 24.sp,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 8.h),
                    Text(
                      'Set up your username and password for secure access',
                      style: TextStyle(
                        color: Colors.grey,
                        fontSize: 14.sp,
                      ),
                    ),
                    SizedBox(height: 30.h),
                    // Username Field
                    CustomTextField(
                      hintText: 'Username',
                      controller: _usernameController,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter a username';
                        }
                        if (value.length < 3) {
                          return 'Username must be at least 3 characters';
                        }
                        return null;
                      },
                    ),
                    SizedBox(height: 16.h),
                    // Password Field
                    CustomTextField(
                      hintText: 'Password',
                      controller: _passwordController,
                      obscureText: _obscurePassword,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter a password';
                        }
                        if (value.length < 6) {
                          return 'Password must be at least 6 characters';
                        }
                        return null;
                      },
                      suffixIcon: IconButton(
                        icon: Icon(
                          _obscurePassword
                              ? Icons.visibility_off
                              : Icons.visibility,
                          color: Colors.grey,
                        ),
                        onPressed: () {
                          setState(() {
                            _obscurePassword = !_obscurePassword;
                          });
                        },
                      ),
                    ),
                    SizedBox(height: 16.h),
                    // Confirm Password Field
                    CustomTextField(
                      hintText: 'Confirm Password',
                      controller: _confirmPasswordController,
                      obscureText: _obscureConfirmPassword,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please confirm your password';
                        }
                        if (value != _passwordController.text) {
                          return 'Passwords do not match';
                        }
                        return null;
                      },
                      suffixIcon: IconButton(
                        icon: Icon(
                          _obscureConfirmPassword
                              ? Icons.visibility_off
                              : Icons.visibility,
                          color: Colors.grey,
                        ),
                        onPressed: () {
                          setState(() {
                            _obscureConfirmPassword = !_obscureConfirmPassword;
                          });
                        },
                      ),
                    ),
                    SizedBox(height: 40.h),
                    // Register Button
                    Center(
                      child: BlocBuilder<AuthBloc, AuthState>(
                        builder: (context, state) {
                          return ElevatedButton(
                            onPressed: state.isLoading
                                ? null
                                : () {
                                    if (_formKey.currentState!.validate()) {
                                      _completeRegistration(context, state);
                                    }
                                  },
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFF1E88E5),
                              foregroundColor: Colors.white,
                              minimumSize: Size(150.w, 50.h),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(25.r),
                              ),
                            ),
                            child: state.isLoading
                                ? SizedBox(
                                    width: 20.w,
                                    height: 20.h,
                                    child: const CircularProgressIndicator(
                                      color: Colors.white,
                                      strokeWidth: 2,
                                    ),
                                  )
                                : Text(
                                    'Complete Registration',
                                    style: TextStyle(fontSize: 16.sp),
                                  ),
                          );
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  void _completeRegistration(BuildContext context, AuthState state) {
    // Get stored temporary data from the state
    final university = state.tempUniversity;
    final college = state.tempCollege;
    final email = state.tempEmail;

    if (university == null || college == null || email == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Registration data is incomplete')),
      );
      return;
    }

    if (widget.isStudent) {
      // For student registration
      final name = state.tempStudentName;
      final batch = state.tempBatch;
      final course = state.tempCourse;
      final rollNumber = state.tempRollNumber;

      if (name == null || batch == null || course == null || rollNumber == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Student data is incomplete')),
        );
        return;
      }

      context.read<AuthBloc>().add(
            StudentRegistrationRequested(
              universityId: university.id,
              collegeId: college.id,
              university: university.name,
              college: college.name,
              email: email,
              name: name,
              batch: batch,
              course: course.name,
              courseId: course.id,
              rollNumber: rollNumber,
              username: _usernameController.text.trim(),
              password: _passwordController.text,
            ),
          );
    } else {
      // For society registration
      final societyName = state.tempSocietyName;
      final presidentName = state.tempPresidentName;

      if (societyName == null || presidentName == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Society data is incomplete')),
        );
        return;
      }

      context.read<AuthBloc>().add(
            SocietyRegistrationRequested(
              universityId: university.id,
              collegeId: college.id,
              university: university.name,
              college: college.name,
              societyName: societyName,
              presidentName: presidentName,
              email: email,
              username: _usernameController.text.trim(),
              password: _passwordController.text,
            ),
          );
    }
  }
}
