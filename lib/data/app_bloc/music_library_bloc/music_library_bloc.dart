import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:meta/meta.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_playlist_type.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';
import 'package:muzik_audio_player/data/database/data_model.dart';

part 'music_library_event.dart';
part 'music_library_state.dart';

class MusicLibraryBloc extends Bloc<MusicLibraryEvent, MusicLibraryState> {
  MusicLibraryBloc() : super(MusicLibraryState.init());

  @override
  Stream<MusicLibraryState> mapEventToState(
    MusicLibraryEvent event,
  ) async* {
    // TODO: implement mapEventToState
  }
}
