import 'package:equatable/equatable.dart';

class Course extends Equatable {
  final String id;
  final String name;
  final String collegeId;
  final String code;
  final int duration; // in years
  final String? description;

  const Course({
    required this.id,
    required this.name,
    required this.collegeId,
    required this.code,
    required this.duration,
    this.description,
  });

  factory Course.fromJson(Map<String, dynamic> json) {
    return Course(
      id: json['id'],
      name: json['name'],
      collegeId: json['collegeId'],
      code: json['code'] ?? '',
      duration: json['duration'] ?? 3,
      description: json['description'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'collegeId': collegeId,
      'code': code,
      'duration': duration,
      'description': description,
    };
  }

  @override
  List<Object?> get props => [id, name, collegeId, code, duration, description];
}
