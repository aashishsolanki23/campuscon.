part of 'deeds_bloc.dart';

abstract class DeedsEvent extends Equatable {
  const DeedsEvent();

  @override
  List<Object?> get props => [];
}

class LoadCollegeDeeds extends DeedsEvent {
  final String collegeName;

  const LoadCollegeDeeds(this.collegeName);

  @override
  List<Object?> get props => [collegeName];
}

class LoadPopularDeeds extends DeedsEvent {
  const LoadPopularDeeds();
}

class LoadUpcomingDeeds extends DeedsEvent {
  const LoadUpcomingDeeds();
}

class LoadCategoryDeeds extends DeedsEvent {
  final String category;

  const LoadCategoryDeeds(this.category);

  @override
  List<Object?> get props => [category];
}

class LoadSocietyDeeds extends DeedsEvent {
  final String societyId;

  const LoadSocietyDeeds(this.societyId);

  @override
  List<Object?> get props => [societyId];
}

class LikeDeed extends DeedsEvent {
  final String deedId;

  const LikeDeed(this.deedId);

  @override
  List<Object?> get props => [deedId];
}

class SaveDeed extends DeedsEvent {
  final String deedId;

  const SaveDeed(this.deedId);

  @override
  List<Object?> get props => [deedId];
}

class RegisterForDeed extends DeedsEvent {
  final String deedId;

  const RegisterForDeed(this.deedId);

  @override
  List<Object?> get props => [deedId];
}

class ShareDeed extends DeedsEvent {
  final String deedId;

  const ShareDeed(this.deedId);

  @override
  List<Object?> get props => [deedId];
}

class LoadMoreDeeds extends DeedsEvent {
  const LoadMoreDeeds();
}
