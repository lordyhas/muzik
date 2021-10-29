part of 'music_library_bloc.dart';

@immutable
abstract class MusicLibraryEvent extends Equatable {}

class MusicLibraryFetchEvent extends MusicLibraryEvent{
  MusicLibraryFetchEvent();

  @override
  List<Object?> get props => [];

}
