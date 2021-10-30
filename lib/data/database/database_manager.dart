import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:just_audio/just_audio.dart';
import 'package:on_audio_query/on_audio_query.dart';
import 'package:path_provider/path_provider.dart';

import '../../objectbox.g.dart';
import 'data_model.dart';

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
    return Store(getObjectBoxModel(), directory: dir.path + '/objectbox');
  }

  Future<void> _initStoreBox() async {
    final store = await openStoreBox;

    final boxRecentState = store.box<RecentState>();
    if(boxRecentState.isEmpty()){
      boxRecentState.put(RecentState());
    }

    store.close();

  }

  void _test() async {
    Store store = await openStoreBox;

    final boxPlaylistItem = store.box<PlaylistInfo>();
    final boxRecentState = store.box<RecentState>();
    store.close();
  }

  void addSongsInActualPlaylist(List<SongModel> songs,{int? currentIndex}) async {
    Store store = await openStoreBox;
    final boxPlaylistItem = store.box<PlaylistInfo>();

    //boxPlaylistItem.putMany().toList());

    print("#### Added :" + songs.toString());

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

    print("##########  length : ${boxPlaylistItem.getAll().length}");
    print("########## songInfo test : ${boxPlaylistItem.getAll()[200].songs}");
    int i = 0;
    boxPlaylistItem.getAll().forEach((e) {
      print("########## songInfo ${i++} : ${e.songs}");

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
    final boxRecentState = store.box<RecentState>();
    final recentState = boxRecentState.query().build().findFirst();
    recentState?.isShuffle = isShuffle ?? recentState.isShuffle;
    recentState?.repeatMode = loopMode?.index ?? recentState.repeatMode;

    boxRecentState.put(recentState!);

    store.close();


  }

   Future<RecentState> getRecentState() async {
    Store store = await openStoreBox;
    final boxRecentState = store.box<RecentState>();
    final recentState = boxRecentState.query().build().findFirst();
    store.close();

    return recentState!;
  }
}

class XDatabaseManager {
  Store? storeManager;

  //DatabaseManager();
  XDatabaseManager.empty();

  /*XDatabaseManager.initStore(){
    //this.storeManager = storeBox;

    this.openStoreBox().then((store) {
      final box = store.box<SettingAppData>();
      if(box.isEmpty()){
        final settingAppData = SettingAppData(
            createAt: new DateTime.now().toString(),
            os: Platform.operatingSystem,
            osVersion: Platform.operatingSystemVersion,
            phoneModel: 'SmartPhone not checked');
        final id = box.put(settingAppData);      // note: sets note.id and also returns it
        print("*** *** *** *** *** ***");
        print('new note got id=$id, which is the same as note.id=${settingAppData.id}');
        print('re-read note: ${box.get(id)}');
        print('Set tingAppData Added Once : ${box.get(id)?.toDisplay()} ###########');

      }
    });
  }*/

  Future<Store> openStoreBox() async {
    Directory dir = await getApplicationDocumentsDirectory();
    if (kIsWeb) {
      return Store(getObjectBoxModel());
    }
    return Store(getObjectBoxModel(), directory: dir.path + '/objectbox');
  }

//Store? get storeManager => this._storeManager;

/*SettingAppData initStoreBoxSetting(){
    final Store store =  openStoreBox();
    final box = store.box<SettingAppData>();

    if(box.isEmpty()){
      final settingAppData = SettingAppData(
          createAt: new DateTime.now().toString(),
          os: Platform.operatingSystem,
          osVersion: Platform.operatingSystemVersion,
          phoneModel: 'SmartPhone not checked');
      final id = box.put(settingAppData);      // note: sets note.id and also returns it

      print('new note got id=$id, which is the same as note.id=${settingAppData.id}');
      print('re-read note: ${box.get(id)}');
      print('Set tingAppData Added Once : ${box.get(id)?.toDisplay()} ###########');

    }
    var settingQuery = box.query().build();

    SettingAppData? settingData = settingQuery.findFirst();
    settingQuery.close();

    return settingData ?? SettingAppData();
  }*/
/*

  Future<SettingAppData?> get getSettingDataBox async {
    Store store = await openStoreBox();
    //final box = store.box<SettingAppData>();
    final settingData = store.box<SettingAppData>()
        .query().build().findFirst();
    //settingData?..theme = themeState.index;
    //final id = box.put(settingData!, mode: PutMode.update);
    store.close();
    return settingData;
  }

  Future<bool> updateSettingData(SettingAppData settingAppData) async {
    bool value = false;
    Store store = await openStoreBox();
    final id = store.box<SettingAppData>()
        .put(settingAppData, mode: PutMode.update);
    if(id != settingAppData.id) value = true;
    store.close();
    return value;
  }


  Future<bool> updateTheme(StylesThemeState themeState) async {
    bool value = false;
    Store store = await openStoreBox();
    final box = store.box<SettingAppData>();
    final settingData = box.query().build().findFirst();
    settingData?..theme = themeState.index;
    final id = box.put(settingData!, mode: PutMode.update);
    if(id != settingData.id) value = true;

    store.close();
    return value;

  }

  Future<bool> updateLanguage(LangState langState) async {
    bool value = false;
    Store store = await openStoreBox();
    final box = store.box<SettingAppData>();
    final settingData = box.query(SettingAppData_.createAt.notNull())
        .build().findFirst();
    settingData?..language = langState.index;
    final id = box.put(settingData!, mode: PutMode.update);
    if(id != settingData.id) value = true;

    store.close();
    return value;

  }

  Future<bool> update_() async {
    bool value = false;
    Store store = await openStoreBox();

    store.close();
    return value;

  }
  Future<bool> add() async {
    bool value = false;
    Store store = await openStoreBox();

    store.close();
    return value;

  }
  Future<void> get_() async {
    bool value = false;
    Store store = await openStoreBox();

    store.close();


  }
*/

}
