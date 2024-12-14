package org.febit.lang.protocol;

import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ListResponseTest {

    record Item(int id, String name) {
    }

    @Test
    void mapping() {
        var response = new ListResponse<Item>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of(
                new Item(1, "name1"),
                new Item(2, "name2")
        ));

        assertThat(response)
                .returns(200, ListResponse::getStatus)
                .returns(true, ListResponse::isSuccess)
                .returns(List.of(
                                new Item(1, "name1"),
                                new Item(2, "name2")
                        ),
                        ListResponse::getData
                );

        assertThat(response.mapEach(Item::name))
                .returns(200, ListResponse::getStatus)
                .returns(true, ListResponse::isSuccess)
                .returns(List.of("name1", "name2"), ListResponse::getData);

        assertThat(new ListResponse<Item>().mapEach(Item::name))
                .returns(0, ListResponse::getStatus)
                .returns(false, ListResponse::isSuccess)
                .returns(null, ListResponse::getData);
    }

    @Test
    void jsonify() {
        String json = """
                {
                    "success": true,
                    "status": 200,
                    "data": [
                        {
                            "id": 1,
                            "name": "name1"
                        },
                        {
                            "id": 2,
                            "name": "name2"
                        }
                    ]
                }
                """;
        var response = JacksonUtils.parse(json,
                JacksonUtils.TYPE_FACTORY.constructParametricType(ListResponse.class, Item.class)
        );
        assertThat(response)
                .asInstanceOf(type(ListResponse.class))
                .returns(200, ListResponse::getStatus)
                .returns(true, ListResponse::isSuccess)
                .returns(List.of(
                                new Item(1, "name1"),
                                new Item(2, "name2")
                        ),
                        ListResponse::getData
                );

        var response2 = JacksonUtils.parse(json,
                JacksonUtils.TYPE_FACTORY.constructParametricType(IListResponse.class, Item.class)
        );

        assertThat(response2)
                .asInstanceOf(type(ListResponse.class))
                .returns(200, IListResponse::getStatus)
                .returns(true, IListResponse::isSuccess)
                .returns(List.of(
                                new Item(1, "name1"),
                                new Item(2, "name2")
                        ),
                        IListResponse::getData
                );
    }

}
