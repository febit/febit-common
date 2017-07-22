/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.util.agent;

import java.io.Serializable;
import org.febit.lang.Function0;

/**
 * 缓存代理. 过期时间后获取实例会重新获取, 如果获取失败, 会沿用上个周期的实例,
 *
 * Notice: 示例生成不能返回null, 否则作为获取失败处理 Notice: 初次构建完可使用 ensure() 方法获取一次实例
 *
 * TODO 增加重试机制
 *
 * @author zqq90
 * @param <T>
 */
public abstract class ExpireUpdateAgent<T> implements Serializable {

    //缺省忍耐失败的次数
    protected static final int DEFAULT_EXTRA_TIMES = 10;

    //实例缓存
    protected transient T instance;

    //单位过期截止时间 unit: ms
    protected final long expire;

    //最大忍耐过期截止时间 unit: ms
    protected final long extraExpire;

    protected long expireTime;
    protected long extraExpireTime;

    protected abstract T create();

    /**
     *
     * @param expire 过期毫秒
     * @param extraExpire 最大忍耐的过期毫秒
     */
    protected ExpireUpdateAgent(long expire, long extraExpire) {
        this.expire = expire;
        this.extraExpire = extraExpire;
    }

    protected ExpireUpdateAgent(long expire) {
        this(expire, expire * DEFAULT_EXTRA_TIMES);
    }

    /**
     * 是否达到过期时间
     *
     * @return
     */
    public boolean isExpire() {
        if (expireTime < 0) {
            return false;
        }
        return expireTime < now();
    }

    /**
     * 是否达到最大忍耐时间
     *
     * @return
     */
    public boolean isExtraExpire() {
        if (extraExpireTime < 0) {
            return false;
        }
        return extraExpireTime < now();
    }

    /**
     * 获取实例. 注意: 根据策略, 可能得到上个周期的旧实例
     *
     * @return
     */
    public T get() {
        return get(false);
    }

    public T getForce(boolean force) {
        return get(true);
    }

    public T get(boolean force) {
        if (!isExpire()) {
            return this.instance;
        }
        return _getOrCreate(force);
    }

    protected synchronized T _getOrCreate(boolean force) {
        T result = this.instance;
        if (!isExpire()) {
            return result;
        }
        RuntimeException exception = null;
        try {
            result = create();
        } catch (RuntimeException e) {
            exception = e;
        }
        //如果结果为 null 抛出异常
        if (result == null) {
            if (exception != null) {
                throw exception;
            }
            throw new RuntimeException("ExpireUpdateAgent got a null instance:" + this.getClass());
        }

        if (exception != null) {
            if (force || isExtraExpire()) {
                throw exception;
            }
            //存在异常, 但是在容错时间内, 可以返回旧有的结果
            return result;
        }

        //获取成功, 缓存并更新时间
        this.instance = result;
        updateExpireTimes(now());
        return result;
    }

    /**
     * 确保实例可用
     *
     * @return
     */
    public ExpireUpdateAgent<T> ensure() {
        if (isExpire()) {
            _getOrCreate(true);
        }
        return this;
    }

    /**
     * 强制标记过期.
     */
    public void expire() {
        this.expireTime = 0;
        this.extraExpireTime = 0;
    }

    /**
     * 强制标记过期并确保实例可用.
     *
     * @return
     */
    public ExpireUpdateAgent<T> expireAndEnsure() {
        expire();
        return ensure();
    }

    /**
     * 更新 `单位过期截止时间` 和 '最大忍耐过期截止时间'
     *
     * @param now
     */
    protected void updateExpireTimes(long now) {
        this.expireTime = (this.expire >= 0) ? (now + this.expire) : -1L;
        this.extraExpireTime = (this.extraExpire >= 0) ? (now + this.extraExpire) : -1L;
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    public static <T> ExpireUpdateAgent<T> create(long expire, long extraExpire, final Function0<T> func) {
        return new ExpireUpdateAgent<T>(expire, extraExpire) {
            @Override
            protected T create() {
                return func.call();
            }
        };
    }

    public static <T> ExpireUpdateAgent<T> create(long expire, final Function0<T> func) {
        return create(expire, expire * DEFAULT_EXTRA_TIMES, func);
    }
}
