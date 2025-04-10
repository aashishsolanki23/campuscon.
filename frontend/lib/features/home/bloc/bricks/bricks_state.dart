part of 'bricks_bloc.dart';

abstract class BricksState extends Equatable {
  const BricksState();
  
  @override
  List<Object?> get props => [];
}

class BricksInitial extends BricksState {
  const BricksInitial();
}

class BricksLoading extends BricksState {}

class BricksLoaded extends BricksState {
  final List<Brick> bricks;
  final bool hasReachedMax;
  final int totalPages;
  final int currentPage;
  final String category;
  final String? query;

  const BricksLoaded({
    required this.bricks,
    required this.hasReachedMax,
    required this.totalPages,
    required this.currentPage,
    required this.category,
    this.query,
  });
  
  @override
  List<Object?> get props => [
    bricks,
    hasReachedMax,
    totalPages,
    currentPage,
    category,
    query,
  ];
}

class BricksError extends BricksState {
  final String message;

  const BricksError({required this.message});
  
  @override
  List<Object> get props => [message];
}
