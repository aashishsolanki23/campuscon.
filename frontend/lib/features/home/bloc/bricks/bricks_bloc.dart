import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../data/models/brick_model.dart';
import '../../../../data/repositories/brick_repository.dart';

part 'bricks_event.dart';
part 'bricks_state.dart';

class BricksBloc extends Bloc<BricksEvent, BricksState> {
  final BrickRepository _brickRepository;

  BricksBloc({required BrickRepository brickRepository})
      : _brickRepository = brickRepository,
        super(const BricksInitial()) {
    on<LoadBricksFeed>(_onLoadBricksFeed);
    on<LoadUserBricks>(_onLoadUserBricks);
    on<LoadSavedBricks>(_onLoadSavedBricks);
    on<LikeBrick>(_onLikeBrick);
    on<SaveBrick>(_onSaveBrick);
    on<CommentOnBrick>(_onCommentOnBrick);
    on<ShareBrick>(_onShareBrick);
    on<LoadMoreBricks>(_onLoadMoreBricks);
  }

  Future<void> _onLoadBricksFeed(
    LoadBricksFeed event,
    Emitter<BricksState> emit,
  ) async {
    emit(BricksLoading());
    try {
      final bricks = await _brickRepository.getBricksFeed();
      emit(BricksLoaded(
        bricks: bricks.content,
        hasReachedMax: bricks.last,
        totalPages: bricks.totalPages,
        currentPage: bricks.page,
        category: 'feed',
      ));
    } catch (e) {
      emit(BricksError(message: e.toString()));
    }
  }

  Future<void> _onLoadUserBricks(
    LoadUserBricks event,
    Emitter<BricksState> emit,
  ) async {
    emit(BricksLoading());
    try {
      final bricks = await _brickRepository.getUserBricks(
        userId: event.userId,
      );
      emit(BricksLoaded(
        bricks: bricks.content,
        hasReachedMax: bricks.last,
        totalPages: bricks.totalPages,
        currentPage: bricks.page,
        category: 'user',
        query: event.userId,
      ));
    } catch (e) {
      emit(BricksError(message: e.toString()));
    }
  }

  Future<void> _onLoadSavedBricks(
    LoadSavedBricks event,
    Emitter<BricksState> emit,
  ) async {
    emit(BricksLoading());
    try {
      final bricks = await _brickRepository.getSavedBricks();
      emit(BricksLoaded(
        bricks: bricks.content,
        hasReachedMax: bricks.last,
        totalPages: bricks.totalPages,
        currentPage: bricks.page,
        category: 'saved',
      ));
    } catch (e) {
      emit(BricksError(message: e.toString()));
    }
  }

  Future<void> _onLikeBrick(
    LikeBrick event,
    Emitter<BricksState> emit,
  ) async {
    if (state is BricksLoaded) {
      final currentState = state as BricksLoaded;
      final brickIndex = currentState.bricks.indexWhere((b) => b.id == event.brickId);
      
      if (brickIndex != -1) {
        try {
          // Optimistic update
          final updatedBricks = List<Brick>.from(currentState.bricks);
          final currentBrick = updatedBricks[brickIndex];
          
          final updatedBrick = Brick(
            id: currentBrick.id,
            title: currentBrick.title,
            description: currentBrick.description,
            imageUrl: currentBrick.imageUrl,
            createdAt: currentBrick.createdAt,
            user: currentBrick.user,
            commentsCount: currentBrick.commentsCount,
            saved: currentBrick.saved,
            liked: !currentBrick.liked,
            likesCount: currentBrick.liked 
                ? currentBrick.likesCount - 1 
                : currentBrick.likesCount + 1,
          );
          
          updatedBricks[brickIndex] = updatedBrick;
          
          emit(BricksLoaded(
            bricks: updatedBricks,
            hasReachedMax: currentState.hasReachedMax,
            totalPages: currentState.totalPages,
            currentPage: currentState.currentPage,
            category: currentState.category,
            query: currentState.query,
          ));
          
          // Make API call
          await _brickRepository.likeBrick(event.brickId);
        } catch (e) {
          emit(BricksError(message: 'Failed to like brick: ${e.toString()}'));
          
          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }

  Future<void> _onSaveBrick(
    SaveBrick event,
    Emitter<BricksState> emit,
  ) async {
    if (state is BricksLoaded) {
      final currentState = state as BricksLoaded;
      final brickIndex = currentState.bricks.indexWhere((b) => b.id == event.brickId);
      
      if (brickIndex != -1) {
        try {
          // Optimistic update
          final updatedBricks = List<Brick>.from(currentState.bricks);
          final currentBrick = updatedBricks[brickIndex];
          
          final updatedBrick = Brick(
            id: currentBrick.id,
            title: currentBrick.title,
            description: currentBrick.description,
            imageUrl: currentBrick.imageUrl,
            createdAt: currentBrick.createdAt,
            user: currentBrick.user,
            likesCount: currentBrick.likesCount,
            commentsCount: currentBrick.commentsCount,
            liked: currentBrick.liked,
            saved: !currentBrick.saved,
          );
          
          updatedBricks[brickIndex] = updatedBrick;
          
          emit(BricksLoaded(
            bricks: updatedBricks,
            hasReachedMax: currentState.hasReachedMax,
            totalPages: currentState.totalPages,
            currentPage: currentState.currentPage,
            category: currentState.category,
            query: currentState.query,
          ));
          
          // Make API call
          await _brickRepository.saveBrick(event.brickId);
        } catch (e) {
          emit(BricksError(message: 'Failed to save brick: ${e.toString()}'));
          
          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }

  Future<void> _onCommentOnBrick(
    CommentOnBrick event,
    Emitter<BricksState> emit,
  ) async {
    if (state is BricksLoaded) {
      final currentState = state as BricksLoaded;
      final brickIndex = currentState.bricks.indexWhere((b) => b.id == event.brickId);
      
      if (brickIndex != -1) {
        try {
          // Optimistic update
          final updatedBricks = List<Brick>.from(currentState.bricks);
          final currentBrick = updatedBricks[brickIndex];
          
          final updatedBrick = Brick(
            id: currentBrick.id,
            title: currentBrick.title,
            description: currentBrick.description,
            imageUrl: currentBrick.imageUrl,
            createdAt: currentBrick.createdAt,
            user: currentBrick.user,
            likesCount: currentBrick.likesCount,
            liked: currentBrick.liked,
            saved: currentBrick.saved,
            commentsCount: currentBrick.commentsCount + 1,
          );
          
          updatedBricks[brickIndex] = updatedBrick;
          
          emit(BricksLoaded(
            bricks: updatedBricks,
            hasReachedMax: currentState.hasReachedMax,
            totalPages: currentState.totalPages,
            currentPage: currentState.currentPage,
            category: currentState.category,
            query: currentState.query,
          ));
          
          // Make API call
          await _brickRepository.commentOnBrick(event.brickId, event.content);
        } catch (e) {
          emit(BricksError(message: 'Failed to comment on brick: ${e.toString()}'));
          
          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }

  Future<void> _onShareBrick(
    ShareBrick event,
    Emitter<BricksState> emit,
  ) async {
    try {
      await _brickRepository.shareBrick(event.brickId);
    } catch (e) {
      if (state is BricksLoaded) {
        emit(BricksError(message: 'Failed to share brick: ${e.toString()}'));
        
        // Revert to previous state
        await Future.delayed(const Duration(milliseconds: 500));
        emit(state);
      }
    }
  }

  Future<void> _onLoadMoreBricks(
    LoadMoreBricks event,
    Emitter<BricksState> emit,
  ) async {
    if (state is BricksLoaded) {
      final currentState = state as BricksLoaded;
      
      if (!currentState.hasReachedMax) {
        try {
          final nextPage = currentState.currentPage + 1;
          BrickResponse response;
          
          switch (currentState.category) {
            case 'feed':
              response = await _brickRepository.getBricksFeed(page: nextPage);
              break;
            case 'user':
              response = await _brickRepository.getUserBricks(
                userId: currentState.query ?? '',
                page: nextPage,
              );
              break;
            case 'saved':
              response = await _brickRepository.getSavedBricks(page: nextPage);
              break;
            default:
              response = await _brickRepository.getBricksFeed(page: nextPage);
          }
          
          emit(BricksLoaded(
            bricks: [...currentState.bricks, ...response.content],
            hasReachedMax: response.last,
            totalPages: response.totalPages,
            currentPage: response.page,
            category: currentState.category,
            query: currentState.query,
          ));
        } catch (e) {
          emit(BricksError(message: 'Failed to load more bricks: ${e.toString()}'));
          
          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }
}
