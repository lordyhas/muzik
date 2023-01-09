import 'dart:collection';
import 'dart:math';


import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';

class Playlist<Song extends SongInfo> extends Object with ListMixin<Song>
    implements List<Song> {

  late final List<Song> innerList;
  late int index;

  Playlist([List<Song> list = const [],int i = 0]):
    innerList = list,
    index = i;


  factory Playlist.empty() => Playlist();
  // ignore: use_function_type_syntax_for_parameters
  factory Playlist.generate(int length, Song generator(int index)){
    if (length <= 0) return Playlist.empty();
    return Playlist(List<Song>.generate(length, generator));
  }

  int get cursor => index;

  set cursor(int index){
    this.index = index;
  }

  @override
  int get length => innerList.length;

  @override
  set length(int  len){
    innerList.length = len;
  }

  @override
  Song operator [](int index) {
    return innerList[index];
  }

  @override
  void operator []=(int index, Song value) {
    innerList[index] = value;
  }

  //void hasNext() => innerList[index] ;
  void addSong(Song element) => super.add(element);
  void addSongs(List<Song> elements) => super.addAll(elements);
  void insertSong(int i, Song element) => super.insert(i,element);
  void insertSongs(int i, List<Song> elements) => super.insertAll(i,elements);

  @override


  bool moveTo(int oldIndex, int newIndex) {
    if(length < newIndex) return false;
    Song element = elementAt(oldIndex);
    super.removeAt(oldIndex);
    insert(newIndex-1, element);
    return true;
  }




}