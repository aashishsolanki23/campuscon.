import 'package:equatable/equatable.dart';

class University extends Equatable {
  final String id;
  final String name;
  final String location;
  final String? logoUrl;

  const University({
    required this.id,
    required this.name,
    required this.location,
    this.logoUrl,
  });

  factory University.fromJson(Map<String, dynamic> json) {
    return University(
      id: json['id'],
      name: json['name'],
      location: json['location'] ?? '',
      logoUrl: json['logoUrl'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'location': location,
      'logoUrl': logoUrl,
    };
  }

  @override
  List<Object?> get props => [id, name, location, logoUrl];
}
