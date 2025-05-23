import React, { useState } from "react";
import { Modal, View, Text, TextInput, TouchableOpacity, ScrollView } from "react-native";
import { styles } from "../ProfilesTab.style";
import { createProfile } from "features/profile/profile.api";
import { toast } from 'react-toastify';

export default function AddProfileModal({ onClose, onProfileCreated }) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const handleSubmit = async () => {
    if (!name.trim()) {
      toast.error("Kötelező megadni profil nevet.");
      return;
    }

    try {
      const newProfile = await createProfile(name, description);
      onProfileCreated(newProfile);
    } catch (error) {
      console.error("Error creating profile:", error);
      toast.error(error.response?.data?.message || "Nem sikerült létrehozni a profilt.");
    }
  };

  return (
    <Modal visible={true} transparent={true} animationType="fade" onRequestClose={onClose}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContainer}>
          <ScrollView contentContainerStyle={styles.modalContent}>
            <Text style={styles.modalTitle}>Új profil létrehozása</Text>

            <TextInput
              style={styles.modalInput}
              placeholder="Profil neve"
              value={name}
              onChangeText={setName}
            />

            <TextInput
              style={styles.modalInput}
              placeholder="Leírás (opcionális)"
              value={description}
              onChangeText={setDescription}
              multiline
            />
          </ScrollView>

          <View style={styles.modalFooter}>
            <View style={styles.modalActions}>
              <TouchableOpacity onPress={onClose}>
                <Text style={styles.cancelButton}>Mégse</Text>
              </TouchableOpacity>

              <TouchableOpacity onPress={handleSubmit}>
                <Text style={styles.saveButton}>Mentés</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </View>
    </Modal>
  );
}