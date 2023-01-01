import 'package:bloc/bloc.dart';
import 'package:meta/meta.dart';

part 'setting_bloc_state.dart';

class SettingCubit extends Cubit<SettingState> {
  SettingCubit() : super(SettingState.initial());

  void save(SettingState settingState){
    emit(state);
  }
}
