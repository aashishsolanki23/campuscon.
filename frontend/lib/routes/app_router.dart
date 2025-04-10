import 'package:auto_route/auto_route.dart';
import 'package:campuscon/features/auth/screens/login/society_login_screen.dart';
import 'package:campuscon/features/auth/screens/login/student_login_screen.dart';
// New registration flow screens
import 'package:campuscon/features/auth/screens/registration/student/student_basic_info_screen.dart';
import 'package:campuscon/features/auth/screens/registration/student/student_personal_info_screen.dart';
import 'package:campuscon/features/auth/screens/registration/society/society_basic_info_screen.dart';
import 'package:campuscon/features/auth/screens/registration/society/society_core_info_screen.dart';
import 'package:campuscon/features/auth/screens/registration/common/credentials_screen.dart';
// Original screens (will be deprecated)
import 'package:campuscon/features/auth/screens/registration/society_registration_screen.dart';
import 'package:campuscon/features/auth/screens/registration/student_registration_screen.dart';
import 'package:campuscon/features/auth/screens/starting_screen.dart';
import 'package:campuscon/features/home/screens/home_screen.dart';

part 'app_router.gr.dart'; // ✅ Important

@AutoRouterConfig(replaceInRouteName: 'Screen,Route')
class AppRouter extends _$AppRouter {
  @override
  List<AutoRoute> get routes => [
    AutoRoute(page: StartingRoute.page, initial: true),
    AutoRoute(page: StudentLoginRoute.page),
    AutoRoute(page: SocietyLoginRoute.page),
    // Old registration routes (will be deprecated)
    AutoRoute(page: StudentRegistrationRoute.page),
    AutoRoute(page: SocietyRegistrationRoute.page),
    // New multi-step registration routes
    AutoRoute(page: StudentBasicInfoRoute.page),
    AutoRoute(page: StudentPersonalInfoRoute.page),
    AutoRoute(page: SocietyBasicInfoRoute.page),
    AutoRoute(page: SocietyCoreInfoRoute.page),
    AutoRoute(page: CredentialsRoute.page),
    AutoRoute(page: HomeRoute.page),
  ];
}
