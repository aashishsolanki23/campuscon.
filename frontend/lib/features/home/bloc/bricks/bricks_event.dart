part of 'bricks_bloc.dart';

abstract class BricksEvent extends Equatable {
  const BricksEvent();

  @override
  List<Object?> get props => [];
}

class LoadBricksFeed extends BricksEvent {
  const LoadBricksFeed();
}

class LoadUserBricks extends BricksEvent {
  final String userId;

  const LoadUserBricks(this.userId);

  @override
  List<Object?> get props => [userId];
}

class LoadSavedBricks extends BricksEvent {
  const LoadSavedBricks();
}

class LikeBrick extends BricksEvent {
  final String brickId;

  const LikeBrick(this.brickId);

  @override
  List<Object?> get props => [brickId];
}

class SaveBrick extends BricksEvent {
  final String brickId;

  const SaveBrick(this.brickId);

  @override
  List<Object?> get props => [brickId];
}

class CommentOnBrick extends BricksEvent {
  final String brickId;
  final String content;

  const CommentOnBrick(this.brickId, this.content);

  @override
  List<Object?> get props => [brickId, content];
}

class ShareBrick extends BricksEvent {
  final String brickId;

  const ShareBrick(this.brickId);

  @override
  List<Object?> get props => [brickId];
}

class LoadMoreBricks extends BricksEvent {
  const LoadMoreBricks();
}
