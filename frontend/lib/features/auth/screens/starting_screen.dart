import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_bloc.dart';
import 'package:campuscon/features/auth/blocs/auth/auth_event.dart';
import 'package:campuscon/routes/app_router.dart';

@RoutePage()
class StartingScreen extends StatelessWidget {
  const StartingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: SafeArea(
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 24.w, vertical: 16.h),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              SizedBox(height: 60.h),
              // Logo
              Center(
                child: Image.asset(
                  'assets/images/campuscon_logo.png',
                  width: 80.w,
                  height: 80.h,
                ),
              ),
              SizedBox(height: 16.h),
              // App Name
              Center(
                child: Text(
                  'Campuscon.',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 28.sp,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              SizedBox(height: 60.h),
              // Tagline
              Text(
                'Unlock your college BLISS!',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 32.sp,
                  fontWeight: FontWeight.bold,
                ),
              ),
              SizedBox(height: 80.h),
              // Student Button
              _buildRoleButton(
                context: context,
                label: 'Student',
                onTap: () {
                  context.read<AuthBloc>().add(const RoleSelected('student'));
                  context.router.push(const StudentLoginRoute());
                },
              ),
              SizedBox(height: 16.h),
              // Society Button
              _buildRoleButton(
                context: context,
                label: 'Society',
                onTap: () {
                  context.read<AuthBloc>().add(const RoleSelected('society'));
                  context.router.push(const SocietyLoginRoute());
                },
              ),
              SizedBox(height: 16.h),
              // Optional Text
              Center(
                child: Text(
                  'Opt. between',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 16.sp,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildRoleButton({
    required BuildContext context,
    required String label,
    required VoidCallback onTap,
  }) {
    return Container(
      height: 60.h,
      decoration: BoxDecoration(
        color: Colors.grey.shade300,
        borderRadius: BorderRadius.circular(30.r),
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(30.r),
          onTap: onTap,
          child: Center(
            child: Text(
              label,
              style: TextStyle(
                color: Colors.black87,
                fontSize: 18.sp,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ),
      ),
    );
  }
}