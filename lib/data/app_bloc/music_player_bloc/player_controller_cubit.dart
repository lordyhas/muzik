import 'package:bloc/bloc.dart';
import 'package:just_audio/just_audio.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_playlist_type.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';
import 'package:muzik_audio_player/data/database/data_model.dart';
import 'package:muzik_audio_player/data/database/database_manager.dart';
import 'package:on_audio_query/on_audio_query.dart';

part 'player_controller_state.dart';

class PlayerControllerBloc extends Cubit<PlayerControllerState> {
  PlayerControllerBloc() : super(PlayerControllerState.init());

  ObjectBoxManager objectBoxManager = ObjectBoxManager();

  AudioPlayer get player => state.player;
  Playlist<SongInfo> get currentPlaylist => state.currentPlaylist;
  Playlist<SongInfo> get queue => currentPlaylist;
  int? get currentIndex =>  state.player.currentIndex;


  Future<void> loadPlaylist(Playlist<SongInfo> songs) async {
    emit(state..currentPlaylist = songs);
  }

  Future<void> play() async {
    emit(state..player.play());
    //state.play();
  }

  Future<void> pause() async {
    emit(state..player.pause());
  }

  Future<void> resume() async {
    state.player.play();
  }

  Future<void> stop() async {
    state.player.stop();
  }

  Future<void> next() async {
    state.player.seekToNext();
  }

  Future<void> prev() async {
    if (state.player.position > const Duration(seconds: 3)) {
      state.player.seek(Duration.zero);
    } else {
      state.player.seekToPrevious();
    }
  }

  Future<void> changeDuration(Duration duration) async {
    //playingDurationCubit.setDuration(duration);
  }

  Future<void> setPosition(Duration position, {int? index}) async {
    emit(state..player.seek(position, index: index));
    //playingPositionCubit.setPosition(position);
  }

  Future<void> playAt({required int index}) async {
    emit(state..player.seek(Duration.zero, index: index));
    emit(state..songIndex = index);
    //playingPositionCubit.setPosition(position);
  }

  Future<void> restoreListSong() async {
    /*List<SongModel> songs = await objectBoxManager.getActualPlaylist();

    int index = await objectBoxManager.recentIndex;
    emit(state..setAudioSource(
        ConcatenatingAudioSource(
            children: songs
                .map((song) => AudioSource.uri(
                    Uri.file(Song(song,).songInfo.data),
                    tag: Song(song).mediaItem))
                .toList()),
        initialIndex: index,
      ));*/



  }

  Future<void> setListSong({
    required List<SongModel> songs,
    int? initialIndex,

  }) async {

    Playlist<SongInfo> playlist = Playlist(songs
        .map((song) => SongInfo.fromModel(song))
        .toList());

    loadPlaylist(playlist);
    emit(state..player.setAudioSource(
      ConcatenatingAudioSource(
          children: playlist
              .map((song) => AudioSource.uri(Uri.file(song.filePath),
              tag: song.mediaItem
          )).toList()),
      initialIndex: initialIndex,
    ));

    //objectBoxManager.addSongsInActualPlaylist(songs);

  }

  Future<void> shuffle(bool enable) async {
    if (enable) {
      //emit(state..player.shuffle()..player.setShuffleModeEnabled(enable));
    }
    //emit(state..player.setShuffleModeEnabled(enable));
  }

  Future<void> repeat() async {}

  //Future<void> shuffle() async {}

  void setIndex() {}
}

