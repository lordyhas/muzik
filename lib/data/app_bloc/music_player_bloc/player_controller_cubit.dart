import 'package:bloc/bloc.dart';
import 'package:just_audio/just_audio.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_playlist_type.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';
import 'package:muzik_audio_player/data/database/database_manager.dart';
import 'package:on_audio_query/on_audio_query.dart';

part 'player_controller_state.dart';

class PlayerController extends Cubit<AudioPlayer> {
  PlayerController() : super(AudioPlayer());

  ObjectBoxManager objectBoxManager = ObjectBoxManager();

  AudioPlayer get player => state;

  Future<void> play() async {
    emit(state..play());
    //state.play();
  }

  Future<void> pause() async {
    emit(state..pause());
  }

  Future<void> resume() async {
    state.play();
  }

  Future<void> stop() async {
    state.stop();
  }

  Future<void> next() async {
    state.seekToNext();
  }

  Future<void> prev() async {
    if (state.position > const Duration(seconds: 3)) {
      state.seek(Duration.zero);
    } else {
      state.seekToPrevious();
    }
  }

  Future<void> changeDuration(Duration duration) async {
    //playingDurationCubit.setDuration(duration);
  }

  Future<void> setPosition(Duration position, {int? index}) async {
    emit(state..seek(position, index: index));
    //playingPositionCubit.setPosition(position);
  }

  Future<void> playAt({required int index}) async {
    emit(state
      ..seek(Duration.zero, index: index)
      ..play());
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
    emit(state..setAudioSource(
      ConcatenatingAudioSource(
          children: songs
              .map((song) => AudioSource.uri(
              Uri.file(SongInfo.fromModel(song,).filePath),
              tag: SongInfo.fromModel(song).mediaItem))
              .toList()),
      initialIndex: initialIndex,
    ));
    //objectBoxManager.addSongsInActualPlaylist(songs);

  }

  Future<void> shuffle(bool enable) async {
    if (enable) {
      emit(state
        ..shuffle()
        ..setShuffleModeEnabled(enable));
    }
    emit(state..setShuffleModeEnabled(enable));
  }

  Future<void> repeat() async {}

  //Future<void> shuffle() async {}

  void setIndex() {}
}

/*
class PlayerControllerCubit extends Cubit<PlayerController> {
  PlayerControllerCubit._() : super(PlayerController(playerState: MusicPlayerState.stopped));

  final AudioPlayer player = AudioPlayer();


  Song? _audioTrack;

  factory PlayerControllerCubit() {
    var mediaPlayer = PlayerControllerCubit._();


    return mediaPlayer;
  }

  Future<void> play() async {
    //emit(PlayerController());
    emit(PlayerController(playerState: MusicPlayerState.playing,song: _audioTrack));
  }

  Future<void> pause() async {
    emit(PlayerController(playerState: MusicPlayerState.paused, song: _audioTrack));

  }

  Future<void> resume() async {
    emit(PlayerController(playerState: MusicPlayerState.playing, song: _audioTrack));

  }

  Future<void> stop() async {
    emit(PlayerController(playerState: MusicPlayerState.stopped, song: _audioTrack));

  }

  Future<void> next() async {}
  Future<void> prev() async {}

  Future<void> changDuration(Duration duration) async {
    //playingDurationCubit.setDuration(duration);
  }

  Future<void> setPosition(Duration position) async {
    //playingPositionCubit.setPosition(position);
  }

}
*/
