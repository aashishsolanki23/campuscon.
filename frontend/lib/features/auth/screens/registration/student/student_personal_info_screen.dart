import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_bloc.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_event.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_state.dart';
import 'package:campuscon/features/auth/models/course_model.dart';
import 'package:campuscon/features/auth/repository/auth_repository.dart';
import 'package:campuscon/features/auth/screens/common/custom_text_field.dart';
import 'package:campuscon/features/auth/screens/common/gradient_button.dart';
import 'package:campuscon/routes/app_router.dart';

@RoutePage()
class StudentPersonalInfoScreen extends StatefulWidget {
  const StudentPersonalInfoScreen({super.key});

  @override
  State<StudentPersonalInfoScreen> createState() => _StudentPersonalInfoScreenState();
}

class _StudentPersonalInfoScreenState extends State<StudentPersonalInfoScreen> {
  final _formKey = GlobalKey<FormState>();
  
  // Controllers for personal info
  final _nameController = TextEditingController();
  final _courseController = TextEditingController();
  final _batchController = TextEditingController();
  final _rollNumberController = TextEditingController();
  
  // Course model
  CourseModel? _selectedCourse;
  
  // Repository for API calls
  late final AuthRepository _authRepository;
  
  @override
  void initState() {
    super.initState();
    _authRepository = context.read<AuthRepository>();
  }
  
  @override
  void dispose() {
    _nameController.dispose();
    _courseController.dispose();
    _batchController.dispose();
    _rollNumberController.dispose();
    super.dispose();
  }
  
  void _showCourseSelectionDialog() async {
    // Get college ID from previous screen via AuthBloc state
    final collegeId = context.read<AuthBloc>().state.tempCollegeId;
    
    if (collegeId == null || collegeId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('College ID is missing. Please restart registration.')),
      );
      return;
    }
    
    // Show loading indicator
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );
    
    try {
      // Fetch courses from API based on selected college
      final courses = await _authRepository.getCourses(
        collegeId: collegeId,
      );
      
      // Close loading dialog
      Navigator.pop(context);
      
      if (courses.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No courses found for this college')),
        );
        return;
      }
      
      // Show selection dialog
      showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            backgroundColor: Colors.grey.shade900,
            title: Text(
              'Select Course',
              style: TextStyle(
                color: Colors.white,
                fontSize: 20.sp,
              ),
            ),
            content: SizedBox(
              width: double.maxFinite,
              height: 300.h,
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: courses.length,
                itemBuilder: (context, index) {
                  final course = courses[index];
                  return ListTile(
                    title: Text(
                      course.name,
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 16.sp,
                      ),
                    ),
                    onTap: () {
                      setState(() {
                        _selectedCourse = course;
                        _courseController.text = course.name;
                      });
                      Navigator.pop(context);
                    },
                  );
                },
              ),
            ),
          );
        },
      );
    } catch (e) {
      // Close loading dialog and show error
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: ${e.toString()}')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state.isAuthenticated) {
          // Navigate to home screen when registration is complete
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
          leading: IconButton(
            icon: const Icon(Icons.arrow_back, color: Colors.white),
            onPressed: () => Navigator.maybePop(context),
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
                    Text(
                      'Complete your\nprofile!',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 28.sp,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 40.h),
                    // Full Name
                    CustomTextField(
                      hintText: 'Full name',
                      controller: _nameController,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter your full name';
                        }
                        return null;
                      },
                    ),
                    SizedBox(height: 16.h),
                    // Course
                    CustomTextField(
                      hintText: 'Select course',
                      controller: _courseController,
                      readOnly: true,
                      onTap: () {
                        // Show course selection dialog
                        _showCourseSelectionDialog();
                      },
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please select your course';
                        }
                        return null;
                      },
                      suffixIcon: const Icon(
                        Icons.arrow_drop_down,
                        color: Colors.grey,
                      ),
                    ),
                    SizedBox(height: 16.h),
                    // Batch Year
                    CustomTextField(
                      hintText: 'Batch year (e.g., 2024)',
                      controller: _batchController,
                      keyboardType: TextInputType.number,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter your batch year';
                        }
                        // Check if it's a valid year
                        final batchYear = int.tryParse(value);
                        if (batchYear == null) {
                          return 'Please enter a valid year';
                        }
                        final currentYear = DateTime.now().year;
                        if (batchYear < currentYear - 10 || batchYear > currentYear + 1) {
                          return 'Please enter a reasonable batch year';
                        }
                        return null;
                      },
                    ),
                    SizedBox(height: 16.h),
                    // Roll Number
                    CustomTextField(
                      hintText: 'Roll number (e.g., 24/CSE/035)',
                      controller: _rollNumberController,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter your roll number';
                        }
                        // Validate roll number format
                        final parts = value.split('/');
                        if (parts.length != 3) {
                          return 'Format should be like: 24/CSE/035';
                        }
                        return null;
                      },
                    ),
                    SizedBox(height: 16.h),
                    SizedBox(height: 40.h),
                    // Register Button
                    Row(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        BlocBuilder<AuthBloc, AuthState>(
                          builder: (context, state) {
                            return GradientButton(
                              onTap: state.isLoading
                                  ? null
                                  : () {
                                      if (_formKey.currentState!.validate()) {
                                         final name = _nameController.text.trim();
                                         final batch = _batchController.text.trim();
                                         final rollNumber = _rollNumberController.text.trim();
                                         
                                         // Validate course selection
                                         if (_selectedCourse == null) {
                                           ScaffoldMessenger.of(context).showSnackBar(
                                             const SnackBar(content: Text('Please select a course')),
                                           );
                                           return;
                                         }
                                        
                                        // Get email from previous screen via AuthBloc state
                                        final email = context.read<AuthBloc>().state.tempEmail;
                                        
                                        if (email == null || email.isEmpty) {
                                          ScaffoldMessenger.of(context).showSnackBar(
                                            const SnackBar(
                                              content: Text('Email information missing. Please restart registration.'),
                                            ),
                                          );
                                          return;
                                        }
                                         
                                         // Store the student info in state
                                         context.read<AuthBloc>().add(
                                           StoreTempData(
                                             tempStudentName: name,
                                             tempBatch: batch,
                                             tempCourse: _selectedCourse, // Pass the Course object
                                             tempRollNumber: rollNumber,
                                           ),
                                         );
                                         
                                         // Navigate to credentials screen
                                         context.router.push(
                                           CredentialsRoute(isStudent: true)
                                         );
                                       }
                                    },
                              child: state.isLoading
                                  ? const SizedBox(
                                      width: 20,
                                      height: 20,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                        color: Colors.white,
                                      ),
                                    )
                                  : Text(
                                      'Continue',
                                      style: TextStyle(
                                        color: Colors.white,
                                        fontSize: 16.sp,
                                        fontWeight: FontWeight.w500,
                                      ),
                                    ),
                            );
                          },
                        ),
                      ],
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
}
