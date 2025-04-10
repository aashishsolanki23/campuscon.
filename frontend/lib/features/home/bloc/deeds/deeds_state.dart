part of 'deeds_bloc.dart';

abstract class DeedsState extends Equatable {
  const DeedsState();
  
  @override
  List<Object?> get props => [];
}

class DeedsInitial extends DeedsState {
  const DeedsInitial();
}

class DeedsLoading extends DeedsState {}

class DeedsLoaded extends DeedsState {
  final List<Deed> deeds;
  final bool hasReachedMax;
  final int totalPages;
  final int currentPage;
  final String category;
  final String? query;

  const DeedsLoaded({
    required this.deeds,
    required this.hasReachedMax,
    required this.totalPages,
    required this.currentPage,
    required this.category,
    this.query,
  });
  
  @override
  List<Object?> get props => [
    deeds,
    hasReachedMax,
    totalPages,
    currentPage,
    category,
    query,
  ];
}

class DeedsError extends DeedsState {
  final String message;

  const DeedsError({required this.message});
  
  @override
  List<Object> get props => [message];
}
