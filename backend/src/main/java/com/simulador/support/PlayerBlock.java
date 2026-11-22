package com.simulador.support;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("player_blocks")
@CompoundIndex(name = "block_pair_unique", def = "{'blockerId': 1, 'blockedId': 1}", unique = true)
public record PlayerBlock(@Id String id, String blockerId, String blockedId, Instant createdAt) {}
