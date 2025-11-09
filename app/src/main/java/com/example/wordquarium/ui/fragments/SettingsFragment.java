package com.example.wordquarium.ui.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.model.PlayerSettingsModel;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.data.repository.PlayerSettingsRepository;
import com.example.wordquarium.ui.RegistrationActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private TextView login;
    private CardView accountExit;
    private CardView bugRep;
    private ImageView picture;
    private Switch mySwitchSound;
    private Switch mySwitchVibration;
    private Switch mySwitchNotification;
    private static final int PICK_IMAGE_REQUEST = 1;
    private PlayerRepository playerRepository;
    private PlayerSettingsRepository playerSettingsRepository;
    public int user_Id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Получаем view-safe ссылки (через root)
        getAllId(view);

        // Player repo
        playerRepository = PlayerRepository.getInstance(getContext());
        int userId = -1;
        if (playerRepository != null) {
            userId = playerRepository.getCurrentUserId();
            PlayerModel player = playerRepository.getUserData(userId);
            if (login != null) {
                login.setSelected(true);
                if (player != null) {
                    login.setText(player.getLogin());
                } else {
                   // login.setText(getString(R.string.default_login_text)); // put a default in strings.xml if you want
                }
            }
        } else {
            Log.w(TAG, "playerRepository is null");
        }

        // Account exit
        if (accountExit != null) {
            accountExit.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), RegistrationActivity.class);
                startActivity(intent);
            });
        }

        // Player settings repo
        playerSettingsRepository = PlayerSettingsRepository.getInstance(getContext());
        int user_Id = playerSettingsRepository.getCurrentUserId();
        PlayerSettingsModel user = playerSettingsRepository.getUserData(user_Id);


        // Настройка переключателей — с проверками null
        if (mySwitchSound != null && user != null) {
            mySwitchSound.setChecked(user.getSound() == 1);
            mySwitchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int newSoundValue = isChecked ? 1 : 0;
                ContentValues values = new ContentValues();
                values.put("sound", newSoundValue);
                playerSettingsRepository.updateUserData(user_Id, values);
            });
        }

        if (mySwitchVibration != null && user != null) {
            mySwitchVibration.setChecked(user.getVibration() == 1);
            mySwitchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int newVibrationValue = isChecked ? 1 : 0;
                ContentValues values = new ContentValues();
                values.put("vibration", newVibrationValue);
                playerSettingsRepository.updateUserData(user_Id, values);
            });
        }

        // Исправленная строка: notification переключатель должен смотреть на поле notification
        if (mySwitchNotification != null && user != null) {
            mySwitchNotification.setChecked(user.getNotification() == 1);
            mySwitchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int newNotificationValue = isChecked ? 1 : 0;
                ContentValues values = new ContentValues();
                values.put("notification", newNotificationValue);
                playerSettingsRepository.updateUserData(user_Id, values);
            });
        }

        // bug report
        if (bugRep != null) {
            bugRep.setOnClickListener(v -> sendEmailToAgency());
        }

        // Оформление и загрузка профиля — проверки на null
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(50);
        drawable.setColor(Color.TRANSPARENT);

        if (picture != null) {
            picture.setBackground(drawable);
            picture.setClipToOutline(true);
            picture.setOnClickListener(v -> openGallery());
            loadProfileImageSafely();
        } else {
            Log.w(TAG, "profile ImageView is null — cannot set background or click listener");
        }

        return view;
    }

    private void getAllId(View view) {
        // Используем root.findViewById — безопасно для фрагмента
        picture = view.findViewById(R.id.profImage);
        accountExit = view.findViewById(R.id.accountExit);
        bugRep = view.findViewById(R.id.bugReport);
        login = view.findViewById(R.id.yourLogin);
        mySwitchSound = view.findViewById(R.id.gameSwitchSound);
        mySwitchVibration = view.findViewById(R.id.gameSwitchVibration);
        mySwitchNotification = view.findViewById(R.id.gameSwitchNotification);
    }

    private static final String AGENCY_EMAIL = "matveicharniauski@gmail.com";

    private void sendEmailToAgency() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + AGENCY_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Сообщение об ошибке");
        intent.putExtra(Intent.EXTRA_TEXT, "Здравствуйте! Хотел бы сообщить об ошибке...");
        startActivity(Intent.createChooser(intent, "Выберите почтовое приложение"));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // startActivityForResult устарел, но оставлю, чтобы минимально менять код
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void loadProfileImageSafely() {
        if (playerSettingsRepository == null) {
            Log.w(TAG, "playerSettingsRepository is null — cannot load profile image");
            if (picture != null) picture.setImageResource(R.drawable.default_profile);
            return;
        }

        int userId = playerSettingsRepository.getCurrentUserId();
        PlayerSettingsModel player = playerSettingsRepository.getUserData(userId);

        if (picture == null) {
            Log.w(TAG, "picture ImageView is null — skipping loadProfileImage");
            return;
        }

        if (player != null && player.getProfileImage() != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(player.getProfileImage(), 0, player.getProfileImage().length);
                picture.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.w(TAG, "Failed to decode profile image: " + e.getMessage());
                picture.setImageResource(R.drawable.default_profile);
            }
        } else {
            picture.setImageResource(R.drawable.default_profile);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                int img_min = Math.min(width, height);
                Bitmap resizedBitmap = cropAndResizeBitmap(bitmap, img_min, img_min);

                saveImageToDatabase(resizedBitmap, img_min);

                if (picture != null) picture.setImageBitmap(resizedBitmap);
            } catch (IOException e) {
                Log.e(TAG, "Failed to get bitmap from gallery: " + e.getMessage());
            }
        }
    }

    private Bitmap cropAndResizeBitmap(Bitmap originalBitmap, int width, int height) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        int cropWidth = Math.min(originalWidth, width);
        int cropHeight = Math.min(originalHeight, height);

        int cropX = (originalWidth - cropWidth) / 2;
        int cropY = (originalHeight - cropHeight) / 2;

        Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, cropX, cropY, cropWidth, cropHeight);

        return Bitmap.createScaledBitmap(croppedBitmap, width, height, true);
    }

    private void saveImageToDatabase(Bitmap bitmap, int size_img) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            if (size_img > 512) {
                int compression = Math.max(10, (int) (1000 / (float) size_img) * 5); // безопасный диапазон
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, compression, byteArrayOutputStream);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.WEBP, compression, byteArrayOutputStream);
                }
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            }

            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            if (playerSettingsRepository != null) {
                int userId = playerSettingsRepository.getCurrentUserId();
                ContentValues values = new ContentValues();
                values.put("profileImage", imageBytes);
                playerSettingsRepository.updateUserData(userId, values);
            } else {
                Log.w(TAG, "playerSettingsRepository is null — cannot save profile image");
            }
        } catch (Exception e) {
            Log.e(TAG, "saveImageToDatabase error: " + e.getMessage());
        }
    }
}
