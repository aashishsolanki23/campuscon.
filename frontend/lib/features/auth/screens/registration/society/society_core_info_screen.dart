import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_bloc.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_event.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_state.dart';
import 'package:campuscon/features/auth/screens/common/custom_text_field.dart';
import 'package:campuscon/features/auth/screens/common/gradient_button.dart';
import 'package:campuscon/routes/app_router.dart';

@RoutePage()
class SocietyCoreInfoScreen extends StatefulWidget {
  const SocietyCoreInfoScreen({super.key});

  @override
  State<SocietyCoreInfoScreen> createState() => _SocietyCoreInfoScreenState();
}

class _SocietyCoreInfoScreenState extends State<SocietyCoreInfoScreen> {
  final _formKey = GlobalKey<FormState>();
  
  // Controllers
  final _presidentNameController = TextEditingController();
  final _societyNameController = TextEditingController();
  
  @override
  void dispose() {
    _presidentNameController.dispose();
    _societyNameController.dispose();
    super.dispose();
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
                      'Complete your\nsociety profile!',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 28.sp,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 40.h),
                    // Society Name
                    CustomTextField(
                      hintText: 'Society name',
                      controller: _societyNameController,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter your society name';
                        }
                        return null;
                      },
                    ),
                    SizedBox(height: 16.h),
                    // President Name
                    CustomTextField(
                      hintText: 'President name',
                      controller: _presidentNameController,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter the president\'s name';
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
                            final authBloc = context.read<AuthBloc>();
                            return GradientButton(
                              onTap: state.isLoading
                                  ? null
                                  : () {
                                      if (_formKey.currentState!.validate()) {
                                        final societyName = _societyNameController.text.trim();
                                        final presidentName = _presidentNameController.text.trim();
                                        
                                        // Store the society name and president name
                                        context.read<AuthBloc>().add(
                                          StoreTempData(
                                            tempSocietyName: societyName,
                                            tempPresidentName: presidentName,
                                          ),
                                        );
                                        
                                        // Navigate to credentials screen
                                        context.router.push(
                                          CredentialsRoute(isStudent: false)
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
