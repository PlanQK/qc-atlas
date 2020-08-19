package org.planqk.atlas.web.dtos;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.planqk.atlas.web.utils.Identifyable;
import org.planqk.atlas.web.utils.ValidationGroups;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

@NoArgsConstructor
@EqualsAndHashCode
@Data
@Relation(itemRelation = "patternRelationType", collectionRelation = "patternRelationTypes")
public class PatternRelationTypeDto implements Identifyable {

    @NotNull(groups = {ValidationGroups.Update.class}, message = "An id is required to perform an update")
    @Null(groups = {ValidationGroups.Create.class}, message = "The id must be null for creating a pattern relation type")
    private UUID id;

    @NotNull(groups = {ValidationGroups.Update.class, ValidationGroups.Create.class},
            message = "Pattern-Relations-Type must have a name!")
    private String name;
}
