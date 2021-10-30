part of data.model;
/*
@JsonSerializable()
@Entity()
class PossessionStats {
  @JsonKey(defaultValue: 0)
  int? id;
  String? cardUuid;
  int? total;
  Map<String, int>? totalByVersion;

  String? get dbTotalByVersion =>
      totalByVersion == null ? null : json.encode(totalByVersion);

  set dbTotalByVersion(String? value) {
    if (value == null) {
      totalByVersion = null;
    } else {
      totalByVersion = Map.from(
          json.decode(value).map((k, v) => MapEntry(k as String, v as int)));
    }
  }
}*/
