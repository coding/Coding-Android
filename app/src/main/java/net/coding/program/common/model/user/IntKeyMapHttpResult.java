package net.coding.program.common.model.user;

import net.coding.program.network.model.HttpResult;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by chenchao on 2017/9/15.
 */

public class IntKeyMapHttpResult extends HttpResult<Map<Integer, String>> implements Serializable {

    private static final long serialVersionUID = -5986977533160105088L;

}
