part of data.model;


@Entity()
class RecentState{
  @Id()
  int uid = 0;
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
  }): repeatMode = loopMode.index ;

  LoopMode get currentLoopMode => LoopMode.values[repeatMode];



}



