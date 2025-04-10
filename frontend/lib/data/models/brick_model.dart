import 'package:equatable/equatable.dart';

class BrickResponse extends Equatable {
  final List<Brick> content;
  final int totalPages;
  final int totalElements;
  final int page;
  final int size;
  final bool last;

  const BrickResponse({
    required this.content,
    required this.totalPages,
    required this.totalElements,
    required this.page,
    required this.size,
    required this.last,
  });

  factory BrickResponse.fromJson(Map<String, dynamic> json) {
    final contentJson = json['content'] as List;
    return BrickResponse(
      content: contentJson.map((brick) => Brick.fromJson(brick)).toList(),
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

class Brick extends Equatable {
  final String id;
  final String title;
  final String description;
  final String imageUrl;
  final DateTime createdAt;
  final User user;
  final int likesCount;
  final int commentsCount;
  final bool liked;
  final bool saved;

  const Brick({
    required this.id,
    required this.title,
    required this.description,
    required this.imageUrl,
    required this.createdAt,
    required this.user,
    required this.likesCount,
    required this.commentsCount,
    required this.liked,
    required this.saved,
  });

  factory Brick.fromJson(Map<String, dynamic> json) {
    return Brick(
      id: json['id'] ?? '',
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      imageUrl: json['imageUrl'] ?? '',
      createdAt: DateTime.parse(json['createdAt'] ?? DateTime.now().toIso8601String()),
      user: User.fromJson(json['user'] ?? {}),
      likesCount: json['likesCount'] ?? 0,
      commentsCount: json['commentsCount'] ?? 0,
      liked: json['liked'] ?? false,
      saved: json['saved'] ?? false,
    );
  }

  @override
  List<Object?> get props => [
        id,
        title,
        description,
        imageUrl,
        createdAt,
        user,
        likesCount,
        commentsCount,
        liked,
        saved,
      ];
}

class User extends Equatable {
  final String id;
  final String name;
  final String username;
  final String profilePictureUrl;
  final String userType; // 'STUDENT' or 'SOCIETY'
  final String college;
  final String university;
  final bool bonded;

  const User({
    required this.id,
    required this.name,
    required this.username,
    required this.profilePictureUrl,
    required this.userType,
    required this.college,
    required this.university,
    required this.bonded,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      username: json['username'] ?? '',
      profilePictureUrl: json['profilePictureUrl'] ?? '',
      userType: json['userType'] ?? 'STUDENT',
      college: json['college'] ?? '',
      university: json['university'] ?? '',
      bonded: json['bonded'] ?? false,
    );
  }

  @override
  List<Object?> get props => [
        id,
        name,
        username,
        profilePictureUrl,
        userType,
        college,
        university,
        bonded,
      ];
}
