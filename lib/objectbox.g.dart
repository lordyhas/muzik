// GENERATED CODE - DO NOT MODIFY BY HAND

// ignore_for_file: camel_case_types

import 'dart:typed_data';

import 'package:objectbox/flatbuffers/flat_buffers.dart' as fb;
import 'package:objectbox/internal.dart'; // generated code can access "internal" functionality
import 'package:objectbox/objectbox.dart';
import 'package:objectbox_flutter_libs/objectbox_flutter_libs.dart';

import 'data/database/model/actual_state_model.dart';

export 'package:objectbox/objectbox.dart'; // so that callers only have to import this file

final _entities = <ModelEntity>[
  ModelEntity(
      id: const IdUid(1, 2510123388441630440),
      name: 'PlaylistItem',
      lastPropertyId: const IdUid(2, 1604655357295170105),
      flags: 0,
      properties: <ModelProperty>[
        ModelProperty(
            id: const IdUid(1, 2948497761108288248),
            name: 'id',
            type: 6,
            flags: 1),
        ModelProperty(
            id: const IdUid(2, 1604655357295170105),
            name: 'recentIndex',
            type: 6,
            flags: 0)
      ],
      relations: <ModelRelation>[],
      backlinks: <ModelBacklink>[]),
  ModelEntity(
      id: const IdUid(2, 747587180024071340),
      name: 'RecentState',
      lastPropertyId: const IdUid(3, 2698552381359974339),
      flags: 0,
      properties: <ModelProperty>[
        ModelProperty(
            id: const IdUid(1, 7343823800555904167),
            name: 'id',
            type: 6,
            flags: 1),
        ModelProperty(
            id: const IdUid(2, 3327378476988274393),
            name: 'isShuffle',
            type: 1,
            flags: 0),
        ModelProperty(
            id: const IdUid(3, 2698552381359974339),
            name: 'repeatMode',
            type: 6,
            flags: 0)
      ],
      relations: <ModelRelation>[],
      backlinks: <ModelBacklink>[])
];

/// Open an ObjectBox store with the model declared in this file.
Future<Store> openStore(
        {String? directory,
        int? maxDBSizeInKB,
        int? fileMode,
        int? maxReaders,
        bool queriesCaseSensitiveDefault = true,
        String? macosApplicationGroup}) async =>
    Store(getObjectBoxModel(),
        directory: directory ?? (await defaultStoreDirectory()).path,
        maxDBSizeInKB: maxDBSizeInKB,
        fileMode: fileMode,
        maxReaders: maxReaders,
        queriesCaseSensitiveDefault: queriesCaseSensitiveDefault,
        macosApplicationGroup: macosApplicationGroup);

/// ObjectBox model definition, pass it to [Store] - Store(getObjectBoxModel())
ModelDefinition getObjectBoxModel() {
  final model = ModelInfo(
      entities: _entities,
      lastEntityId: const IdUid(2, 747587180024071340),
      lastIndexId: const IdUid(0, 0),
      lastRelationId: const IdUid(0, 0),
      lastSequenceId: const IdUid(0, 0),
      retiredEntityUids: const [],
      retiredIndexUids: const [],
      retiredPropertyUids: const [],
      retiredRelationUids: const [],
      modelVersion: 5,
      modelVersionParserMinimum: 5,
      version: 1);

  final bindings = <Type, EntityDefinition>{
    PlaylistItem: EntityDefinition<PlaylistItem>(
        model: _entities[0],
        toOneRelations: (PlaylistItem object) => [],
        toManyRelations: (PlaylistItem object) => {},
        getId: (PlaylistItem object) => object.id,
        setId: (PlaylistItem object, int id) {
          object.id = id;
        },
        objectToFB: (PlaylistItem object, fb.Builder fbb) {
          fbb.startTable(3);
          fbb.addInt64(0, object.id);
          fbb.addInt64(1, object.recentIndex);
          fbb.finish(fbb.endTable());
          return object.id;
        },
        objectFromFB: (Store store, ByteData fbData) {
          final buffer = fb.BufferContext(fbData);
          final rootOffset = buffer.derefObject(0);

          final object = PlaylistItem(
              recentIndex:
                  const fb.Int64Reader().vTableGet(buffer, rootOffset, 6, 0))
            ..id = const fb.Int64Reader().vTableGet(buffer, rootOffset, 4, 0);

          return object;
        }),
    RecentState: EntityDefinition<RecentState>(
        model: _entities[1],
        toOneRelations: (RecentState object) => [],
        toManyRelations: (RecentState object) => {},
        getId: (RecentState object) => object.id,
        setId: (RecentState object, int id) {
          object.id = id;
        },
        objectToFB: (RecentState object, fb.Builder fbb) {
          fbb.startTable(4);
          fbb.addInt64(0, object.id);
          fbb.addBool(1, object.isShuffle);
          fbb.addInt64(2, object.repeatMode);
          fbb.finish(fbb.endTable());
          return object.id;
        },
        objectFromFB: (Store store, ByteData fbData) {
          final buffer = fb.BufferContext(fbData);
          final rootOffset = buffer.derefObject(0);

          final object = RecentState(
              isShuffle:
                  const fb.BoolReader().vTableGet(buffer, rootOffset, 6, false))
            ..id = const fb.Int64Reader().vTableGet(buffer, rootOffset, 4, 0)
            ..repeatMode =
                const fb.Int64Reader().vTableGet(buffer, rootOffset, 8, 0);

          return object;
        })
  };

  return ModelDefinition(model, bindings);
}

/// [PlaylistItem] entity fields to define ObjectBox queries.
class PlaylistItem_ {
  /// see [PlaylistItem.id]
  static final id =
      QueryIntegerProperty<PlaylistItem>(_entities[0].properties[0]);

  /// see [PlaylistItem.recentIndex]
  static final recentIndex =
      QueryIntegerProperty<PlaylistItem>(_entities[0].properties[1]);
}

/// [RecentState] entity fields to define ObjectBox queries.
class RecentState_ {
  /// see [RecentState.id]
  static final id =
      QueryIntegerProperty<RecentState>(_entities[1].properties[0]);

  /// see [RecentState.isShuffle]
  static final isShuffle =
      QueryBooleanProperty<RecentState>(_entities[1].properties[1]);

  /// see [RecentState.repeatMode]
  static final repeatMode =
      QueryIntegerProperty<RecentState>(_entities[1].properties[2]);
}
