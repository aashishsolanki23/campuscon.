import 'package:equatable/equatable.dart';

class College extends Equatable {
  final String id;
  final String name;
  final String universityId;
  final String location;
  final String? logoUrl;
  final String? emailDomain;

  const College({
    required this.id,
    required this.name,
    required this.universityId,
    required this.location,
    this.logoUrl,
    this.emailDomain,
  });

  factory College.fromJson(Map<String, dynamic> json) {
    return College(
      id: json['id'],
      name: json['name'],
      universityId: json['universityId'],
      location: json['location'] ?? '',
      logoUrl: json['logoUrl'],
      emailDomain: json['emailDomain'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'universityId': universityId,
      'location': location,
      'logoUrl': logoUrl,
      'emailDomain': emailDomain,
    };
  }

  @override
  List<Object?> get props => [id, name, universityId, location, logoUrl, emailDomain];
}
