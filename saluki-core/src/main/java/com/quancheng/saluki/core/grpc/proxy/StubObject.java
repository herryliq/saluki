package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public class StubObject<T> extends AbstractProtocolProxy<T> {

    public StubObject(String protocol, Callable<Channel> channelCallable, int rpcTimeout, int callType,
                      boolean isGeneric){
        super(protocol, channelCallable, rpcTimeout, callType, isGeneric);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getProxy() {
        String protocol = getProtocol();
        if (StringUtils.contains(protocol, "$")) {
            try {
                String parentName = StringUtils.substringBefore(protocol, "$");
                Class<?> clzz = ReflectUtil.name2class(parentName);
                Method method;
                switch (this.getCallType()) {
                    case SalukiConstants.RPCTYPE_ASYNC:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                    case SalukiConstants.RPCTYPE_BLOCKING:
                        method = clzz.getMethod("newBlockingStub", io.grpc.Channel.class);
                        break;
                    default:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                }
                T value = (T) method.invoke(null, getChannel());
                return value;
            } catch (Exception e) {
                throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file", e);
            }
        } else {
            throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file");
        }
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> buildMethodDescriptor(Method method,
                                                                                             Object[] args) {
        return null;
    }

}