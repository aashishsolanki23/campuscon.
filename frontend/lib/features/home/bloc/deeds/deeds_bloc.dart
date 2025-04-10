import 'package:equatable/equatable.dart';
import 'dart:async';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:bloc_concurrency/bloc_concurrency.dart';

import '../../../../data/models/deed_model.dart';
import '../../../../data/repositories/deed_repository.dart';

part 'deeds_event.dart';
part 'deeds_state.dart';

class DeedsBloc extends Bloc<DeedsEvent, DeedsState> {
  final DeedRepository _deedRepository;

  DeedsBloc({required DeedRepository deedRepository})
    : _deedRepository = deedRepository,
      super(const DeedsInitial()) {
    on<LoadCollegeDeeds>(_onLoadCollegeDeeds, transformer: droppable());
    on<LoadPopularDeeds>(_onLoadPopularDeeds, transformer: droppable());
    on<LoadSocietyDeeds>(_onLoadSocietyDeeds, transformer: droppable());
    on<LoadUpcomingDeeds>(_onLoadUpcomingDeeds, transformer: droppable());
    on<LoadCategoryDeeds>(_onLoadCategoryDeeds);
    on<LoadSocietyDeeds>(_onLoadSocietyDeeds);
    on<LikeDeed>(_onLikeDeed);
    on<SaveDeed>(_onSaveDeed);
    on<RegisterForDeed>(_onRegisterForDeed);
    on<ShareDeed>(_onShareDeed);
    on<LoadMoreDeeds>(_onLoadMoreDeeds);
  }

  Future<void> _onLoadCollegeDeeds(
    LoadCollegeDeeds event,
    Emitter<DeedsState> emit,
  ) async {
    emit(DeedsLoading());
    try {
      final deeds = await _deedRepository.getCollegeDeeds(
        collegeName: event.collegeName,
      );
      emit(
        DeedsLoaded(
          deeds: deeds.content,
          hasReachedMax: deeds.last,
          totalPages: deeds.totalPages,
          currentPage: deeds.page,
          category: 'college',
          query: event.collegeName,
        ),
      );
    } catch (e) {
      emit(DeedsError(message: e.toString()));
    }
  }

  Future<void> _onLoadPopularDeeds(
    LoadPopularDeeds event,
    Emitter<DeedsState> emit,
  ) async {
    emit(DeedsLoading());
    try {
      final deeds = await _deedRepository.getPopularDeeds();
      emit(
        DeedsLoaded(
          deeds: deeds.content,
          hasReachedMax: deeds.last,
          totalPages: deeds.totalPages,
          currentPage: deeds.page,
          category: 'popular',
        ),
      );
    } catch (e) {
      emit(DeedsError(message: e.toString()));
    }
  }

  Future<void> _onLoadUpcomingDeeds(
    LoadUpcomingDeeds event,
    Emitter<DeedsState> emit,
  ) async {
    emit(DeedsLoading());
    try {
      final deeds = await _deedRepository.getUpcomingDeeds();
      emit(
        DeedsLoaded(
          deeds: deeds.content,
          hasReachedMax: deeds.last,
          totalPages: deeds.totalPages,
          currentPage: deeds.page,
          category: 'upcoming',
        ),
      );
    } catch (e) {
      emit(DeedsError(message: e.toString()));
    }
  }

  Future<void> _onLoadCategoryDeeds(
    LoadCategoryDeeds event,
    Emitter<DeedsState> emit,
  ) async {
    emit(DeedsLoading());
    try {
      final deeds = await _deedRepository.getDeedsByCategory(
        category: event.category,
      );
      emit(
        DeedsLoaded(
          deeds: deeds.content,
          hasReachedMax: deeds.last,
          totalPages: deeds.totalPages,
          currentPage: deeds.page,
          category: 'category',
          query: event.category,
        ),
      );
    } catch (e) {
      emit(DeedsError(message: e.toString()));
    }
  }

  Future<void> _onLoadSocietyDeeds(
    LoadSocietyDeeds event,
    Emitter<DeedsState> emit,
  ) async {
    emit(DeedsLoading());
    try {
      final deeds = await _deedRepository.getSocietyDeeds(
        societyId: event.societyId,
      );
      emit(
        DeedsLoaded(
          deeds: deeds.content,
          hasReachedMax: deeds.last,
          totalPages: deeds.totalPages,
          currentPage: deeds.page,
          category: 'society',
          query: event.societyId,
        ),
      );
    } catch (e) {
      emit(DeedsError(message: e.toString()));
    }
  }

  Future<void> _onLikeDeed(LikeDeed event, Emitter<DeedsState> emit) async {
    if (state is DeedsLoaded) {
      final currentState = state as DeedsLoaded;
      final deedIndex = currentState.deeds.indexWhere(
        (d) => d.id == event.deedId,
      );

      if (deedIndex != -1) {
        try {
          // Optimistic update
          final updatedDeeds = List<Deed>.from(currentState.deeds);
          final currentDeed = updatedDeeds[deedIndex];

          final updatedDeed = Deed(
            id: currentDeed.id,
            title: currentDeed.title,
            description: currentDeed.description,
            bannerUrl: currentDeed.bannerUrl,
            venue: currentDeed.venue,
            category: currentDeed.category,
            eventDate: currentDeed.eventDate,
            society: currentDeed.society,
            commentsCount: currentDeed.commentsCount,
            registrationsCount: currentDeed.registrationsCount,
            saved: currentDeed.saved,
            registered: currentDeed.registered,
            liked: !currentDeed.liked,
            likesCount:
                currentDeed.liked
                    ? currentDeed.likesCount - 1
                    : currentDeed.likesCount + 1,
          );

          updatedDeeds[deedIndex] = updatedDeed;

          emit(
            DeedsLoaded(
              deeds: updatedDeeds,
              hasReachedMax: currentState.hasReachedMax,
              totalPages: currentState.totalPages,
              currentPage: currentState.currentPage,
              category: currentState.category,
              query: currentState.query,
            ),
          );

          // Make API call
          await _deedRepository.likeDeed(event.deedId);
        } catch (e) {
          emit(DeedsError(message: 'Failed to like deed: ${e.toString()}'));

          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }

  Future<void> _onSaveDeed(SaveDeed event, Emitter<DeedsState> emit) async {
    if (state is DeedsLoaded) {
      final currentState = state as DeedsLoaded;
      final deedIndex = currentState.deeds.indexWhere(
        (d) => d.id == event.deedId,
      );

      if (deedIndex != -1) {
        try {
          // Optimistic update
          final updatedDeeds = List<Deed>.from(currentState.deeds);
          final currentDeed = updatedDeeds[deedIndex];

          final updatedDeed = Deed(
            id: currentDeed.id,
            title: currentDeed.title,
            description: currentDeed.description,
            bannerUrl: currentDeed.bannerUrl,
            venue: currentDeed.venue,
            category: currentDeed.category,
            eventDate: currentDeed.eventDate,
            society: currentDeed.society,
            likesCount: currentDeed.likesCount,
            commentsCount: currentDeed.commentsCount,
            registrationsCount: currentDeed.registrationsCount,
            liked: currentDeed.liked,
            registered: currentDeed.registered,
            saved: !currentDeed.saved,
          );

          updatedDeeds[deedIndex] = updatedDeed;

          emit(
            DeedsLoaded(
              deeds: updatedDeeds,
              hasReachedMax: currentState.hasReachedMax,
              totalPages: currentState.totalPages,
              currentPage: currentState.currentPage,
              category: currentState.category,
              query: currentState.query,
            ),
          );

          // Make API call
          await _deedRepository.saveDeed(event.deedId);
        } catch (e) {
          emit(DeedsError(message: 'Failed to save deed: ${e.toString()}'));

          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }

  Future<void> _onRegisterForDeed(
    RegisterForDeed event,
    Emitter<DeedsState> emit,
  ) async {
    if (state is DeedsLoaded) {
      final currentState = state as DeedsLoaded;
      final deedIndex = currentState.deeds.indexWhere(
        (d) => d.id == event.deedId,
      );

      if (deedIndex != -1) {
        try {
          // Optimistic update
          final updatedDeeds = List<Deed>.from(currentState.deeds);
          final currentDeed = updatedDeeds[deedIndex];

          final updatedDeed = Deed(
            id: currentDeed.id,
            title: currentDeed.title,
            description: currentDeed.description,
            bannerUrl: currentDeed.bannerUrl,
            venue: currentDeed.venue,
            category: currentDeed.category,
            eventDate: currentDeed.eventDate,
            society: currentDeed.society,
            likesCount: currentDeed.likesCount,
            commentsCount: currentDeed.commentsCount,
            liked: currentDeed.liked,
            saved: currentDeed.saved,
            registered: !currentDeed.registered,
            registrationsCount:
                currentDeed.registered
                    ? currentDeed.registrationsCount - 1
                    : currentDeed.registrationsCount + 1,
          );

          updatedDeeds[deedIndex] = updatedDeed;

          emit(
            DeedsLoaded(
              deeds: updatedDeeds,
              hasReachedMax: currentState.hasReachedMax,
              totalPages: currentState.totalPages,
              currentPage: currentState.currentPage,
              category: currentState.category,
              query: currentState.query,
            ),
          );

          // Make API call
          await _deedRepository.registerForDeed(event.deedId);
        } catch (e) {
          emit(
            DeedsError(message: 'Failed to register for deed: ${e.toString()}'),
          );

          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }

  Future<void> _onShareDeed(ShareDeed event, Emitter<DeedsState> emit) async {
    try {
      await _deedRepository.shareDeed(event.deedId);
    } catch (e) {
      if (state is DeedsLoaded) {
        emit(DeedsError(message: 'Failed to share deed: ${e.toString()}'));

        // Revert to previous state
        await Future.delayed(const Duration(milliseconds: 500));
        emit(state);
      }
    }
  }

  Future<void> _onLoadMoreDeeds(
    LoadMoreDeeds event,
    Emitter<DeedsState> emit,
  ) async {
    if (state is DeedsLoaded) {
      final currentState = state as DeedsLoaded;

      if (!currentState.hasReachedMax) {
        try {
          final nextPage = currentState.currentPage + 1;
          DeedResponse response;

          switch (currentState.category) {
            case 'college':
              response = await _deedRepository.getCollegeDeeds(
                collegeName: currentState.query ?? '',
                page: nextPage,
              );
              break;
            case 'popular':
              response = await _deedRepository.getPopularDeeds(page: nextPage);
              break;
            case 'upcoming':
              response = await _deedRepository.getUpcomingDeeds(page: nextPage);
              break;
            case 'category':
              response = await _deedRepository.getDeedsByCategory(
                category: currentState.query ?? '',
                page: nextPage,
              );
              break;
            case 'society':
              response = await _deedRepository.getSocietyDeeds(
                societyId: currentState.query ?? '',
                page: nextPage,
              );
              break;
            default:
              response = await _deedRepository.getPopularDeeds(page: nextPage);
          }

          emit(
            DeedsLoaded(
              deeds: [...currentState.deeds, ...response.content],
              hasReachedMax: response.last,
              totalPages: response.totalPages,
              currentPage: response.page,
              category: currentState.category,
              query: currentState.query,
            ),
          );
        } catch (e) {
          emit(
            DeedsError(message: 'Failed to load more deeds: ${e.toString()}'),
          );

          // Revert to previous state
          await Future.delayed(const Duration(milliseconds: 500));
          emit(currentState);
        }
      }
    }
  }
}
