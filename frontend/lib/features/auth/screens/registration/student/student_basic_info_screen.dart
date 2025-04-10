import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_bloc.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_event.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_state.dart';
import 'package:campuscon/features/auth/models/university_model.dart';
import 'package:campuscon/features/auth/models/college_model.dart';
import 'package:campuscon/features/auth/repository/auth_repository.dart';
import 'package:campuscon/features/auth/screens/common/custom_text_field.dart';
import 'package:campuscon/features/auth/screens/common/gradient_button.dart';
import 'package:campuscon/routes/app_router.dart';
import 'package:pin_code_fields/pin_code_fields.dart';

@RoutePage()
class StudentBasicInfoScreen extends StatefulWidget {
  const StudentBasicInfoScreen({super.key});

  @override
  State<StudentBasicInfoScreen> createState() => _StudentBasicInfoScreenState();
}

class _StudentBasicInfoScreenState extends State<StudentBasicInfoScreen> {
  final _formKey = GlobalKey<FormState>();
  
  // Controllers for university, college, email
  final _universityController = TextEditingController();
  final _collegeController = TextEditingController();
  final _emailController = TextEditingController();
  final _otpController = TextEditingController();
  
  // Selected entities
  University? _selectedUniversity;
  College? _selectedCollege;
  
  // OTP verification flag
  bool _isVerifyingOtp = false;
  
  // Repository for API calls
  late final AuthRepository _authRepository;
  
  @override
  void initState() {
    super.initState();
    _authRepository = context.read<AuthRepository>();
  }
  
  @override
  void dispose() {
    _universityController.dispose();
    _collegeController.dispose();
    _emailController.dispose();
    _otpController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state.isOtpVerified) {
          // Navigate to the next screen for additional details
          context.router.push(const StudentPersonalInfoRoute());
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
                      'Register your\naccount!',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 28.sp,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 40.h),
                    // University Selection
                    CustomTextField(
                      hintText: 'Select university',
                      controller: _universityController,
                      readOnly: true,
                      onTap: () {
                        // Show university selection dialog
                        _showUniversitySelectionDialog();
                      },
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please select your university';
                        }
                        return null;
                      },
                      suffixIcon: const Icon(
                        Icons.arrow_drop_down,
                        color: Colors.grey,
                      ),
                    ),
                    SizedBox(height: 16.h),
                    // College Selection
                    CustomTextField(
                      hintText: 'Select college',
                      controller: _collegeController,
                      readOnly: true,
                      onTap: () {
                        // Show college selection dialog
                        _showCollegeSelectionDialog();
                      },
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please select your college';
                        }
                        return null;
                      },
                      suffixIcon: const Icon(
                        Icons.arrow_drop_down,
                        color: Colors.grey,
                      ),
                    ),
                    SizedBox(height: 16.h),
                    // Email
                    CustomTextField(
                      hintText: 'College email',
                      controller: _emailController,
                      keyboardType: TextInputType.emailAddress,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter your college email';
                        }
                        if (!value.contains('@')) {
                          return 'Please enter a valid email address';
                        }
                        return null;
                      },
                    ),
                    SizedBox(height: 24.h),
                    
                    // OTP Verification
                    if (_isVerifyingOtp) ...[
                      Text(
                        'Enter OTP sent to your email',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 16.sp,
                        ),
                      ),
                      SizedBox(height: 16.h),
                      PinCodeTextField(
                        appContext: context,
                        length: 6,
                        obscureText: false,
                        animationType: AnimationType.fade,
                        pinTheme: PinTheme(
                          shape: PinCodeFieldShape.box,
                          borderRadius: BorderRadius.circular(15),
                          fieldHeight: 50,
                          fieldWidth: 40,
                          activeFillColor: Colors.grey.shade800,
                          inactiveFillColor: Colors.grey.shade800,
                          selectedFillColor: Colors.grey.shade700,
                          activeColor: Colors.blue,
                          inactiveColor: Colors.grey.shade600,
                          selectedColor: Colors.blue,
                        ),
                        cursorColor: Colors.white,
                        animationDuration: const Duration(milliseconds: 300),
                        enableActiveFill: true,
                        keyboardType: TextInputType.number,
                        onCompleted: (v) {
                          // Verify OTP
                          context.read<AuthBloc>().add(
                            VerifyOTPRequested(
                              email: _emailController.text.trim(),
                              otp: v,
                            ),
                          );
                        },
                        onChanged: (value) {},
                        beforeTextPaste: (text) {
                          return true;
                        },
                      ),
                    ],
                    SizedBox(height: 24.h),
                    // Submit Button
                    Row(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        BlocBuilder<AuthBloc, AuthState>(
                          builder: (context, state) {
                            return GradientButton(
                              onTap: state.isLoading ? null : () {
                                if (_isVerifyingOtp) {
                                  if (_otpController.text.length == 6) {
                                    context.read<AuthBloc>().add(
                                      VerifyOTPRequested(
                                        email: _emailController.text.trim(),
                                        otp: _otpController.text,
                                      ),
                                    );
                                  }
                                } else {
                                  if (_formKey.currentState!.validate()) {
                                    // First validate if email domain matches college domain
                                    if (_selectedCollege != null) {
                                      _validateAndSendOTP();
                                    } else {
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        const SnackBar(content: Text('Please select a college first')),
                                      );
                                    }
                                  }
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
                                      _isVerifyingOtp ? 'Verify' : 'Continue',
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
  
  Future<void> _validateAndSendOTP() async {
    final email = _emailController.text.trim();
    final collegeId = _selectedCollege!.id;
    
    // Show loading indicator
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );
    
    try {
      // Validate email domain against college domain
      final isValid = await _authRepository.validateEmailDomain(
        email: email,
        collegeId: collegeId,
      );
      
      // Close loading dialog
      Navigator.pop(context);
      
      if (isValid) {
        // Send OTP if email domain is valid
        context.read<AuthBloc>().add(SendOTPRequested(email: email));
        setState(() {
          _isVerifyingOtp = true;
        });
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Invalid email domain for ${_selectedCollege!.name}')),
        );
      }
    } catch (e) {
      // Close loading dialog and show error
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: ${e.toString()}')),
      );
    }
  }
  
  void _showUniversitySelectionDialog() async {
    // Show loading indicator
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );
    
    try {
      // Fetch universities from API
      final universities = await _authRepository.getUniversities();
      
      // Close loading dialog
      Navigator.pop(context);
      
      if (universities.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No universities found')),
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
              'Select University',
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
                itemCount: universities.length,
                itemBuilder: (context, index) {
                  final university = universities[index];
                  return ListTile(
                    title: Text(
                      university.name,
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 16.sp,
                      ),
                    ),
                    subtitle: Text(
                      university.location,
                      style: TextStyle(
                        color: Colors.grey.shade400,
                        fontSize: 14.sp,
                      ),
                    ),
                    onTap: () {
                      setState(() {
                        _selectedUniversity = university;
                        _universityController.text = university.name;
                        // Reset college when university changes
                        _collegeController.clear();
                        _selectedCollege = null;
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
  
  void _showCollegeSelectionDialog() async {
    if (_selectedUniversity == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a university first')),
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
      // Fetch colleges from API based on selected university
      final colleges = await _authRepository.getColleges(
        universityId: _selectedUniversity!.id,
      );
      
      // Close loading dialog
      Navigator.pop(context);
      
      if (colleges.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No colleges found for this university')),
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
              'Select College',
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
                itemCount: colleges.length,
                itemBuilder: (context, index) {
                  final college = colleges[index];
                  return ListTile(
                    title: Text(
                      college.name,
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 16.sp,
                      ),
                    ),
                    subtitle: Text(
                      college.location,
                      style: TextStyle(
                        color: Colors.grey.shade400,
                        fontSize: 14.sp,
                      ),
                    ),
                    onTap: () {
                      setState(() {
                        _selectedCollege = college;
                        _collegeController.text = college.name;
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
}
