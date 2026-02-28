import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:just_audio/just_audio.dart';
import 'package:objectbox/objectbox.dart';
import 'package:on_audio_query/on_audio_query.dart';
import 'package:path_provider/path_provider.dart';

import 'package:muzik_audio_player/data/database/data_model.dart';

abstract class DatabaseManager {
  const DatabaseManager();
}

class ObjectBoxManager extends DatabaseManager {
  ObjectBoxManager();
  ObjectBoxManager.init() {
    _initStoreBox();
  }

  Future<Store> get openStoreBox async  {
    Directory dir = await getApplicationDocumentsDirectory();
    //return await openStore(directory: dir.path + '/objectbox');
    //return Store(getObjectBoxModel(), directory: dir.path + '/objectbox');
    throw UnimplementedError();

  }

  Future<void> _initStoreBox() async {
    final store = await openStoreBox;

    final boxRecentState = store.box<ControllerState>();
    if(boxRecentState.isEmpty()){
      boxRecentState.put(ControllerState());
    }

    store.close();

  }

  void _test() async {
    Store store = await openStoreBox;

    final boxPlaylistItem = store.box<PlaylistInfo>();
    final boxRecentState = store.box<ControllerState>();
    store.close();
  }

  void addSongsInActualPlaylist(List<SongModel> songs,{int? currentIndex}) async {
    Store store = await openStoreBox;
    final boxPlaylistItem = store.box<PlaylistInfo>();

    //boxPlaylistItem.putMany().toList());

    print("#### Added :$songs");

    store.close();

    //boxPlaylistItem.close();
  }

  Future<List<SongModel>> _getActualPlaylist() async {
    Store store = await openStoreBox;
    final boxPlaylistItem = store.box<PlaylistInfo>();

    if(boxPlaylistItem.isEmpty()) {
      store.close();
      return  const[];
    }

    print('##########  length : ${boxPlaylistItem.getAll().length}');
    print('########## songInfo test : ${boxPlaylistItem.getAll()[200].songs}');
    int i = 0;
    boxPlaylistItem.getAll().forEach((e) {
      print('########## songInfo ${i++} : ${e.songs}');

    });


    if(boxPlaylistItem.getAll().isEmpty){

    }

    //final queryNullText = boxPlaylistItem.query(PlaylistItem_..isNull()).build();

    //assert(boxPlaylistItem.count() == 3);             // executes the query, returns int
    //final notesWithNullText = queryNullText.find();

    /*List<SongModel> songs = boxPlaylistItem.getAll().map((e) {
      print("########## : ${e.songModel.title}");
      print("########## : ${e.songModel}");
      return e.songModel;
    }).toList();*/

    store.close();

    return [];

  }

  Future<int> getRecentIndex() async {
    Store store = await openStoreBox;
    final boxPlaylistItem = store.box<PlaylistInfo>();
    final int index =  boxPlaylistItem.query().build().findFirst()!.recentIndex;
    store.close();

    return index;

  }

  Future<int> get recentIndex  => getRecentIndex();

  void saveActualState({bool? isShuffle, LoopMode? loopMode}) async {
    Store store = await openStoreBox;
    final boxRecentState = store.box<ControllerState>();
    final recentState = boxRecentState.query().build().findFirst();
    recentState?.isShuffle = isShuffle ?? recentState.isShuffle;
    recentState?.repeatMode = loopMode?.index ?? recentState.repeatMode;

    boxRecentState.put(recentState!);

    store.close();


  }

   Future<ControllerState> getRecentState() async {
    Store store = await openStoreBox;
    final boxRecentState = store.box<ControllerState>();
    final recentState = boxRecentState.query().build().findFirst();
    store.close();

    return recentState!;
  }
}

class XDatabaseManager {
  Store? storeManager;

  //DatabaseManager();
  XDatabaseManager.empty();


  Future<void> openStoreBox() async {
    Directory dir = await getApplicationDocumentsDirectory();
    if (kIsWeb) {
      //return Store(getObjectBoxModel());
    }
    //return Store(getObjectBoxModel(), directory: dir.path + '/objectbox');

  }

}
