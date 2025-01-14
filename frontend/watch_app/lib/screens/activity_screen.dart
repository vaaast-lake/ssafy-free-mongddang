import 'package:flutter/material.dart';
import 'dart:async';
import 'package:dio/dio.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import '../models/activity.dart';
import 'result_screen.dart';

class ActivityScreen extends StatefulWidget {
  final ActivityType type;

  const ActivityScreen({
    super.key,
    required this.type,
  });

  @override
  State<ActivityScreen> createState() => _ActivityScreenState();
}

class _ActivityScreenState extends State<ActivityScreen> {
  Timer? _timer;
  int _minutes = 0;
  int _seconds = 0;
  bool _showQuestion = false;
  final dio = Dio();

  @override
  void initState() {
    super.initState();
    _initializeScreen();
  }

  void _initializeScreen() async {
    if (widget.type == ActivityType.sleep) {
      await _startSleep();
    }
    _startTimer();
  }

  Future<void> _startSleep() async {
    try {
      dio.options.headers = {
        'Authorization': 'Bearer ${dotenv.env['ACCESS_TOKEN']}',
        'Content-Type': 'application/json',
      };

      final response = await dio.post(
        '${dotenv.env['BASE_URL']}/api/record/sleep/start',
      );

      if (response.statusCode == 200) {
        print('수면 시작 API 호출 성공');
      } else {
        print('수면 시작 API 호출 실패: ${response.statusCode}');
      }
    } catch (e) {
      print('수면 시작 API 호출 오류: $e');
    }
  }

  Future<void> _endSleep() async {
    try {
      dio.options.headers = {
        'Authorization': 'Bearer ${dotenv.env['ACCESS_TOKEN']}',
        'Content-Type': 'application/json',
      };

      final response = await dio.patch(
        '${dotenv.env['BASE_URL']}/api/record/sleep/end',
      );

      if (response.statusCode == 200) {
        print('수면 종료 API 호출 성공');
      } else {
        print('수면 종료 API 호출 실패: ${response.statusCode}');
      }
    } catch (e) {
      print('수면 종료 API 호출 오류: $e');
    }
  }

  void _startTimer() {
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (mounted) {
        setState(() {
          _seconds++;
          if (_seconds >= 60) {
            _minutes++;
            _seconds = 0;
          }
        });
      }
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Activity get _activity => Activity.activities[widget.type]!;

  @override
  Widget build(BuildContext context) {
    if (_showQuestion) {
      return _buildQuestionScreen();
    }

    return _buildTimerScreen();
  }

  Widget _buildTimerScreen() {
    final minutes = _minutes.toString().padLeft(2, '0');
    final seconds = _seconds.toString().padLeft(2, '0');

    return Scaffold(
      backgroundColor: Colors.black,
      body: Center(
        child: Container(
          width: MediaQuery.of(context).size.width,
          height: MediaQuery.of(context).size.height,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Image.asset(
                _activity.imagePath,
                width: 40,
                height: 40,
                errorBuilder: (context, error, stackTrace) {
                  return Container(
                    width: 40,
                    height: 40,
                    color: Colors.transparent,
                  );
                },
              ),
              const SizedBox(height: 12),
              Text(
                _activity.activeTitle,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                '$minutes:$seconds',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 20),
              GestureDetector(
                onTap: () {
                  setState(() {
                    _showQuestion = true;
                  });
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: const Text(
                    '끝',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildQuestionScreen() {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Center(
        child: Container(
          width: MediaQuery.of(context).size.width,
          height: MediaQuery.of(context).size.height,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Image.asset(
                _activity.imagePath,
                width: 40,
                height: 40,
                errorBuilder: (context, error, stackTrace) {
                  return Container(
                    width: 40,
                    height: 40,
                    color: Colors.transparent,
                  );
                },
              ),
              const SizedBox(height: 16),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Text(
                  _activity.question,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _buildAnswerButton(
                    text: '아니',
                    color: Colors.red,
                    onTap: () {
                      setState(() {
                        _showQuestion = false;
                      });
                    },
                  ),
                  const SizedBox(width: 16),
                  _buildAnswerButton(
                    text: '응',
                    color: Colors.blue,
                    onTap: () async {
                      _timer?.cancel();
                      if (widget.type == ActivityType.sleep) {
                        await _endSleep();
                      }
                      Navigator.pushReplacement(
                        context,
                        MaterialPageRoute(
                          builder: (context) => ResultScreen(type: widget.type),
                        ),
                      );
                    },
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAnswerButton({
    required String text,
    required Color color,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(
          text,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 14,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}