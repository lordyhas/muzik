import 'package:just_audio/just_audio.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';
import 'package:objectbox/objectbox.dart';
import 'package:on_audio_query/on_audio_query.dart';



@Entity()
class RecentState{
  int id = 0;
  //
  bool isShuffle;
  /// enum LoopMode { off, one, all }
  /// 0 : off
  /// 1 : one
  /// 2 : all
  int repeatMode;

  RecentState({
    LoopMode loopMode = LoopMode.off,
    this.isShuffle = false,
  }): this.repeatMode = loopMode.index ;

  LoopMode get currentLoopMode => LoopMode.values[repeatMode];



}

@Entity()
class PlaylistItem {
  int id = 0;
  int recentIndex;

  Map<String, Object> songInfo;
  PlaylistItem({
    this.recentIndex = 0,
    this.songInfo = const <String, Object>{} ,
  });

  factory PlaylistItem.fromModel(SongModel songModel, {required int index}) => PlaylistItem(
    songInfo: songModel.getMap as Map<String, Object>,
    recentIndex: index,
  );

  @Transient()
  SongModel get songModel => SongModel(songInfo);
}

