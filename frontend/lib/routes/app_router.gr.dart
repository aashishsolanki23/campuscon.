// GENERATED CODE - DO NOT MODIFY BY HAND

// **************************************************************************
// AutoRouterGenerator
// **************************************************************************

// ignore_for_file: type=lint
// coverage:ignore-file

part of 'app_router.dart';

abstract class _$AppRouter extends RootStackRouter {
  // ignore: unused_element
  _$AppRouter();

  @override
  final Map<String, PageFactory> pagesMap = {
    HomeRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const HomeScreen(),
      );
    },
    SocietyLoginRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const SocietyLoginScreen(),
      );
    },
    SocietyRegistrationRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const SocietyRegistrationScreen(),
      );
    },
    StartingRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const StartingScreen(),
      );
    },
    StudentLoginRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const StudentLoginScreen(),
      );
    },
    StudentRegistrationRoute.name: (routeData) {
      return AutoRoutePage<dynamic>(
        routeData: routeData,
        child: const StudentRegistrationScreen(),
      );
    },
  };
}

/// generated route for
/// [HomeScreen]
class HomeRoute extends PageRouteInfo<void> {
  const HomeRoute({List<PageRouteInfo>? children})
    : super(HomeRoute.name, initialChildren: children);

  static const String name = 'HomeRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [SocietyLoginScreen]
class SocietyLoginRoute extends PageRouteInfo<void> {
  const SocietyLoginRoute({List<PageRouteInfo>? children})
    : super(SocietyLoginRoute.name, initialChildren: children);

  static const String name = 'SocietyLoginRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [SocietyRegistrationScreen]
class SocietyRegistrationRoute extends PageRouteInfo<void> {
  const SocietyRegistrationRoute({List<PageRouteInfo>? children})
    : super(SocietyRegistrationRoute.name, initialChildren: children);

  static const String name = 'SocietyRegistrationRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [StartingScreen]
class StartingRoute extends PageRouteInfo<void> {
  const StartingRoute({List<PageRouteInfo>? children})
    : super(StartingRoute.name, initialChildren: children);

  static const String name = 'StartingRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [StudentLoginScreen]
class StudentLoginRoute extends PageRouteInfo<void> {
  const StudentLoginRoute({List<PageRouteInfo>? children})
    : super(StudentLoginRoute.name, initialChildren: children);

  static const String name = 'StudentLoginRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}

/// generated route for
/// [StudentRegistrationScreen]
class StudentRegistrationRoute extends PageRouteInfo<void> {
  const StudentRegistrationRoute({List<PageRouteInfo>? children})
    : super(StudentRegistrationRoute.name, initialChildren: children);

  static const String name = 'StudentRegistrationRoute';

  static const PageInfo<void> page = PageInfo<void>(name);
}
