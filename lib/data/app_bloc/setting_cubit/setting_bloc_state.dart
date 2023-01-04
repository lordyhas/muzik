part of 'setting_bloc_cubit.dart';



class SettingState {
  double speed;
  Duration sleep;
  int crossFade;
  bool mono;
  bool notification;
  bool displayAsGrid;
  bool coverBehind;
  dynamic syncMode;

  SettingState._({
    this.speed = 0.5,
    this.sleep = Duration.zero,
    this.crossFade = 0,
    this.mono = false,
    this.notification = false,
    this.displayAsGrid = true,
    this.coverBehind = false,
    this.syncMode,
  });

  SettingState.initial() : this._();

  Map<String, dynamic> toMap() {
    return {
      'speed': speed,
      'sleep': sleep,
      'crossFade': crossFade,
      'mono': mono,
      'notification': notification,
      'displayAsGrid': displayAsGrid,
      'showCoverInBackground': coverBehind,
      'syncMode': syncMode,
    };
  }

  factory SettingState.fromMap(Map<String, dynamic> map) {
    return SettingState._(
      speed: map['speed'] as double,
      sleep: map['sleep'] as Duration,
      crossFade: map['crossFade'] as int,
      mono: map['mono'] as bool,
      notification: map['notification'] as bool,
      displayAsGrid: map['displayAsGrid'] as bool,
      coverBehind: map['showCoverInBackground'] as bool,
      syncMode: map['syncMode'] as dynamic,
    );
  }
}
