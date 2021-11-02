part of 'music_library_bloc.dart';

@immutable
abstract class MusicLibraryEvent extends Equatable {
  const MusicLibraryEvent();
}

class FetchPlaylistEvent extends MusicLibraryEvent{
  const FetchPlaylistEvent();

  @override
  List<Object?> get props => [];

}
class SavePlaylistEvent extends MusicLibraryEvent{
  final PlaylistInfo playlistInfo;
  const SavePlaylistEvent(this.playlistInfo);

  @override
  List<Object?> get props => [playlistInfo];
}