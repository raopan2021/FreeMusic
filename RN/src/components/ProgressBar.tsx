/**
 * 进度条组件
 */

import React, {useState, useRef} from 'react';
import {
  View,
  Text,
  StyleSheet,
  PanResponder,
  LayoutChangeEvent,
} from 'react-native';

interface ProgressBarProps {
  position: number; // 当前进度（毫秒）
  duration: number; // 总时长（毫秒）
  onSeek?: (position: number) => void; // 拖动时回调
  showTime?: boolean;
  barHeight?: number;
  activeColor?: string;
  inactiveColor?: string;
}

export default function ProgressBar({
  position,
  duration,
  onSeek,
  showTime = true,
  barHeight = 4,
  activeColor = '#6366F1',
  inactiveColor = '#333',
}: ProgressBarProps): React.JSX.Element {
  const [containerWidth, setContainerWidth] = useState(0);
  const isDragging = useRef(false);
  const dragPosition = useRef(0);

  const formatTime = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  const progress = duration > 0 ? (isDragging.current ? dragPosition.current : position) / duration : 0;

  const handleLayout = (event: LayoutChangeEvent) => {
    setContainerWidth(event.nativeEvent.layout.width);
  };

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: (_, gestureState) => {
        isDragging.current = true;
        if (containerWidth > 0 && duration > 0) {
          dragPosition.current = (gestureState.x0 / containerWidth) * duration;
        }
      },
      onPanResponderMove: (_, gestureState) => {
        if (containerWidth > 0) {
          const newPosition = Math.max(
            0,
            Math.min(
              duration,
              (gestureState.x0 + gestureState.dx) / containerWidth * duration,
            ),
          );
          dragPosition.current = newPosition;
        }
      },
      onPanResponderRelease: () => {
        isDragging.current = false;
        if (onSeek && containerWidth > 0) {
          onSeek(dragPosition.current);
        }
      },
    }),
  ).current;

  return (
    <View style={styles.container}>
      <View
        style={styles.barContainer}
        onLayout={handleLayout}
        {...panResponder.panHandlers}>
        <View style={[styles.bar, {height: barHeight, backgroundColor: inactiveColor}]}>
          <View
            style={[
              styles.progress,
              {
                width: `${Math.min(100, progress * 100)}%`,
                backgroundColor: activeColor,
              },
            ]}
          />
        </View>
      </View>
      {showTime && (
        <View style={styles.timeContainer}>
          <Text style={styles.time}>{formatTime(position)}</Text>
          <Text style={styles.time}>{formatTime(duration)}</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
  barContainer: {
    width: '100%',
    paddingVertical: 8,
  },
  bar: {
    width: '100%',
    borderRadius: 2,
    overflow: 'hidden',
  },
  progress: {
    height: '100%',
    borderRadius: 2,
  },
  timeContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 4,
  },
  time: {
    fontSize: 12,
    color: '#666',
  },
});
