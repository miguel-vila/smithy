/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.model.traits;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.ExamplesTrait.ErrorExample;
import software.amazon.smithy.model.traits.ExamplesTrait.Example;

public class ExamplesTraitTest {
    @Test
    public void loadsTrait() {
        TraitFactory provider = TraitFactory.createServiceFactory();
        ArrayNode node = Node.arrayNode(
                Node.objectNode()
                        .withMember("title", Node.from("foo")),
                Node.objectNode()
                        .withMember("title", Node.from("qux"))
                        .withMember("documentation", Node.from("docs"))
                        .withMember("input", Node.objectNode().withMember("a", Node.from("b")))
                        .withMember("output", Node.objectNode().withMember("c", Node.from("d")))
                        .withMember("error", Node.objectNode()
                                .withMember(Node.from("shapeId"), Node.from("smithy.example#FooError"))
                                .withMember(Node.from("content"), Node.objectNode().withMember("e", Node.from("f")))));

        Optional<Trait> trait = provider.createTrait(
                ShapeId.from("smithy.api#examples"), ShapeId.from("ns.qux#foo"), node);
        assertTrue(trait.isPresent());
        assertThat(trait.get(), instanceOf(ExamplesTrait.class));
        ExamplesTrait examples = (ExamplesTrait) trait.get();

        assertThat(examples.toNode(), equalTo(node));
        assertThat(examples.toBuilder().build(), equalTo(examples));
    }

    @Test
    public void exampleEqualsWorks() {
        ObjectNode input = Node.objectNode().withMember("a", Node.from("b"));
        ObjectNode output = Node.objectNode().withMember("c", Node.from("d"));
        ErrorExample errorExample1 = ErrorExample.builder().shapeId(ShapeId.from("smithy.example#FooError")).content(Node.objectNode()
                .withMember("e", Node.from("f"))).build();
        ErrorExample errorExample2 = ErrorExample.builder().shapeId(ShapeId.from("smithy.example#FooError")).content(Node.objectNode()
                .withMember("e", Node.from("f"))).build();
        Example example1 = Example.builder().title("foo").documentation("docs").input(input).output(output).error(errorExample1).build();
        Example example2 = Example.builder().title("foo").documentation("docs").input(input).output(output).error(errorExample2).build();
        assertThat(errorExample1, equalTo(errorExample2));
        assertThat(example1, equalTo(example2));
    }

}
