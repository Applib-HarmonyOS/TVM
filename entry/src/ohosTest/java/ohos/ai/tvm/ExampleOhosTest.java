package ohos.ai.tvm;

import ohos.app.Context;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import org.apache.tvm.*;

import ohos.aafwk.ability.delegation.AbilityDelegatorRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ExampleOhosTest {
    @Test
    public void testBundleName() {
        final String actualBundleName = AbilityDelegatorRegistry.getArguments().getTestBundleName();
        assertEquals("ohos.ai.tvm", actualBundleName);
    }

    @Test
    public void test_from_int8() {
        NDArray ndarray = NDArray.empty(new long[]{2, 2}, new TVMType("int8"));
        ndarray.copyFrom(new byte[]{1, 2, 3, 4});
        assertArrayEquals(new byte[]{1, 2, 3, 4}, ndarray.asByteArray());
        ndarray.release();
    }

    @Test
    public void test_load_add_func_cpu() {
        Context mContext = AbilityDelegatorRegistry.getAbilityDelegator().getAppContext();
        RawFileEntry rawfile = mContext.getResourceManager().getRawFileEntry("entry/resources/rawfile/add_cpu.so");

        Module fadd = null;
        try {
            File file = getFileFromRawFile("add_cpu.so", rawfile, mContext.getCacheDir());
            fadd = Module.load(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Device dev = new Device("cpu", 0);
        long[] shape = new long[]{2};
        NDArray arr = NDArray.empty(shape, dev);

        arr.copyFrom(new float[]{3f, 4f});

        NDArray res = NDArray.empty(shape, dev);

        fadd.entryFunc().pushArg(arr).pushArg(arr).pushArg(res).invoke();
        assertArrayEquals(new float[]{6f, 8f}, res.asFloatArray(), 1e-3f);

        // test call() api
        fadd.entryFunc().call(arr, arr, res);
        assertArrayEquals(new float[]{6f, 8f}, res.asFloatArray(), 1e-3f);

        arr.release();
        res.release();
        fadd.release();
    }

    private static File getFileFromRawFile(String filename, RawFileEntry rawFileEntry, File cacheDir)
            throws IOException {
        byte[] buf = null;
        File file;
        FileOutputStream output = null;

        try {
            file = new File(cacheDir, filename);
            output = new FileOutputStream(file);
            Resource resource = rawFileEntry.openRawFile();
            buf = new byte[(int) rawFileEntry.openRawFileDescriptor().getFileSize()];
            int bytesRead = resource.read(buf);
            if (bytesRead != buf.length) {
                throw new IOException("Asset Read failed!!!");
            }

            output.write(buf, 0, bytesRead);

            return file;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            output.close();
        }
    }

}