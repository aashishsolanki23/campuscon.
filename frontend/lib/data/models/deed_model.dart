import 'package:equatable/equatable.dart';

class DeedResponse extends Equatable {
  final List<Deed> content;
  final int totalPages;
  final int totalElements;
  final int page;
  final int size;
  final bool last;

  const DeedResponse({
    required this.content,
    required this.totalPages,
    required this.totalElements,
    required this.page,
    required this.size,
    required this.last,
  });

  factory DeedResponse.fromJson(Map<String, dynamic> json) {
    final contentJson = json['content'] as List;
    return DeedResponse(
      content: contentJson.map((deed) => Deed.fromJson(deed)).toList(),
      totalPages: json['totalPages'] ?? 0,
      totalElements: json['totalElements'] ?? 0,
      page: json['page'] ?? 0,
      size: json['size'] ?? 0,
      last: json['last'] ?? false,
    );
  }

  @override
  List<Object?> get props => [content, totalPages, totalElements, page, size, last];
}

class Deed extends Equatable {
  final String id;
  final String title;
  final String description;
  final String bannerUrl;
  final String venue;
  final String category;
  final DateTime eventDate;
  final Society society;
  final int likesCount;
  final int commentsCount;
  final int registrationsCount;
  final bool liked;
  final bool saved;
  final bool registered;

  const Deed({
    required this.id,
    required this.title,
    required this.description,
    required this.bannerUrl,
    required this.venue,
    required this.category,
    required this.eventDate,
    required this.society,
    required this.likesCount,
    required this.commentsCount,
    required this.registrationsCount,
    required this.liked,
    required this.saved,
    required this.registered,
  });

  factory Deed.fromJson(Map<String, dynamic> json) {
    return Deed(
      id: json['id'] ?? '',
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      bannerUrl: json['bannerUrl'] ?? '',
      venue: json['venue'] ?? '',
      category: json['category'] ?? '',
      eventDate: DateTime.parse(json['eventDate'] ?? DateTime.now().toIso8601String()),
      society: Society.fromJson(json['society'] ?? {}),
      likesCount: json['likesCount'] ?? 0,
      commentsCount: json['commentsCount'] ?? 0,
      registrationsCount: json['registrationsCount'] ?? 0,
      liked: json['liked'] ?? false,
      saved: json['saved'] ?? false,
      registered: json['registered'] ?? false,
    );
  }

  @override
  List<Object?> get props => [
        id,
        title,
        description,
        bannerUrl,
        venue,
        category,
        eventDate,
        society,
        likesCount,
        commentsCount,
        registrationsCount,
        liked,
        saved,
        registered,
      ];
}

class Society extends Equatable {
  final String id;
  final String name;
  final String profilePictureUrl;
  final String college;
  final String university;
  final bool bonded;

  const Society({
    required this.id,
    required this.name,
    required this.profilePictureUrl,
    required this.college,
    required this.university,
    required this.bonded,
  });

  factory Society.fromJson(Map<String, dynamic> json) {
    return Society(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      profilePictureUrl: json['profilePictureUrl'] ?? '',
      college: json['college'] ?? '',
      university: json['university'] ?? '',
      bonded: json['bonded'] ?? false,
    );
  }

  @override
  List<Object?> get props => [id, name, profilePictureUrl, college, university, bonded];
}
