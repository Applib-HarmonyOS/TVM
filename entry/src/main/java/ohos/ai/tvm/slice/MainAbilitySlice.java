package ohos.ai.tvm.slice;

import ohos.ai.tvm.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import org.apache.tvm.Device;
import org.apache.tvm.LogUtil;
import org.apache.tvm.Module;
import org.apache.tvm.NDArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainAbilitySlice extends AbilitySlice {
    private static final String TAG = MainAbilitySlice.class.getName();

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        RawFileEntry rawfile = getResourceManager().getRawFileEntry("entry/resources/rawfile/add_cpu.so");
        Module fadd = null;
        try {
            File file = getFileFromRawFile("add_cpu.so", rawfile, getCacheDir());
            fadd = Module.load(file.getAbsolutePath());
        } catch (IOException e) {
            LogUtil.error(TAG, e.getMessage());
        }

        Device dev = new Device("cpu", 0);
        long[] shape = new long[]{2};
        NDArray arr = NDArray.empty(shape, dev);

        arr.copyFrom(new float[]{3f, 4f});

        NDArray res = NDArray.empty(shape, dev);

        fadd.entryFunc().pushArg(arr).pushArg(arr).pushArg(res).invoke();

        // call() api
        fadd.entryFunc().call(arr, arr, res);

        arr.release();
        res.release();
        fadd.release();

        LogUtil.info(TAG, "Prediction finished!!!");

        super.setUIContent(ResourceTable.Layout_ability_main);
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
                throw new IOException("Jai Hanuman!!! Asset Read failed!!!");
            }

            //output = new FileOutputStream(file);
            output.write(buf, 0, bytesRead);

            return file;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            output.close();
        }
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
